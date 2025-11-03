async function fetchBookingHistory() {
  const res = await fetch("/api/booking-history");
  return await res.json();
}

function getStatusText(status) {
  switch (status) {
    case 'saved': return 'บันทึกแล้ว';
    case 'rejected': return 'ไม่อนุมัติ';
    case 'cancelled': return 'ยกเลิกแล้ว';
    case 'approved': return 'อนุมัติแล้ว';
    case 'inactive': return 'ไม่มีบทบาท';
    default: return '-';
  }
}

function renderTimeline(steps) {
  return steps.map(step => {
    let cls = 'timeline-step';
    if (step === 'approved') cls += ' approved';
    else if (step === 'rejected' || step === 'cancelled') cls += ' rejected';
    else if (step === steps[steps.length - 1]) cls += ' active';
    return `<div class="${cls}">- ${getStatusText(step)}</div>`;
  }).join('');
}

function renderRequests(data) {
  const container = document.getElementById('request-list');
  data.forEach((req, index) => {
    const item = document.createElement('div');
    item.className = 'request-item';

    item.innerHTML = `
      <span>${req.date}</span>
      <span>${req.id}</span>
      <span>${req.room}</span>
      <span>${req.time}</span>
      <span class="status ${req.status}">${getStatusText(req.status)}</span>
      <button onclick="toggleDetails(this, ${index})">▼</button>
    `;

    container.appendChild(item);
  });
}

function toggleDetails(btn, index) {
  const parent = btn.closest('.request-item');
  const existing = parent.nextElementSibling;
  if (existing && existing.classList.contains(`details`)) {
    existing.remove();
    parent.classList.remove('highlight');
    return;
  }

  parent.classList.add('highlight');
  const req = window.__bookingData[index];

  const detail = document.createElement('div');
  detail.className = `details`;

  detail.innerHTML = `
    <div class="section-title">รายละเอียดคำร้อง</div>
    <div class="info">ชื่อ: ${req.name}</div>
    <div class="info">อีเมล: ${req.email}</div>
    <div class="info">เบอร์โทรศัพท์: ${req.phone}</div>
    <div class="info">วันที่ต้องการใช้ห้อง: ${req.requestDate}</div>
    <div class="info">ช่วงเวลา: ${req.time}</div>
    <div class="info">ห้อง: ${req.room}</div>
    <div class="info">ประเภทห้อง: ${req.type}</div>

    <div class="section-title">สถานะ</div>
    <div class="timeline">${renderTimeline(req.steps)}</div>
  `;

  parent.after(detail);
}

(async function init() {
  const data = await fetchBookingHistory();
  window.__bookingData = data;
  renderRequests(data);
})();
