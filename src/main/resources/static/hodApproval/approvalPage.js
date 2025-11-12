
// ถ้ามีไฟล์แนบ ให้เซ็ต href ที่นี่
// document.querySelector(".file-pill").href = "https://your-storage/xxx.pdf";

// ===== Elements
const backdrop = document.getElementById('backdrop');
const approveModal = document.getElementById('approveModal');
const rejectModal  = document.getElementById('rejectModal');
const approveBtn   = document.getElementById('approveBtn');
const rejectBtn    = document.getElementById('rejectBtn');
const approveConfirmBtn = document.getElementById('approveConfirmBtn');
const rejectConfirmBtn  = document.getElementById('rejectConfirmBtn');
const rejectReason = document.getElementById('rejectReason');

let lastFocusedEl = null;

function openModal(modalEl) {
  lastFocusedEl = document.activeElement;

  // โชว์ element ก่อน เพื่อให้ transition ทำงาน
  backdrop.hidden = false;
  modalEl.hidden = false;

  // ใส่คลาส .show ในเฟรมถัดไป (ทริกเกอร์ transition)
  requestAnimationFrame(() => {
    backdrop.classList.add('show');
    modalEl.classList.add('show');
    document.body.classList.add('modal-open');
  });

  // โฟกัสตัวแรกในโมดัล
  const firstFocus =
    modalEl.querySelector('[data-close]') ||
    modalEl.querySelector('button, [href], textarea, input');
  firstFocus?.focus();

  document.addEventListener('keydown', onEscToClose);

  // ป้องกัน listener ซ้อน: ผูกแบบ once
  backdrop.addEventListener('click', closeAll, { once: true });
  modalEl.querySelectorAll('[data-close]')
    .forEach(b => b.addEventListener('click', closeAll, { once: true }));
}

function closeAll() {
  // เอา .show ออกเพื่อเล่น transition ย้อนกลับ
  backdrop.classList.remove('show');
  approveModal.classList.remove('show');
  rejectModal.classList.remove('show');
  document.body.classList.remove('modal-open');
  document.removeEventListener('keydown', onEscToClose);

  // รอให้ transition จบก่อนค่อยซ่อนจริง
  const DURATION = 250; // ต้องสอดคล้องกับเวลาใน CSS
  setTimeout(() => {
    backdrop.hidden = true;
    approveModal.hidden = true;
    rejectModal.hidden = true;
    lastFocusedEl?.focus();
  }, DURATION);
}


function onEscToClose(e) {
  if (e.key === 'Escape') closeAll();
}

// ===== Wire buttons
approveBtn.addEventListener('click', () => openModal(approveModal));
rejectBtn.addEventListener('click', () => {
  rejectReason.classList.remove('is-invalid');
  rejectReason.value = '';
  openModal(rejectModal);
});

// ===== Confirm actions (รอเชื่อม API จริง)
approveConfirmBtn.addEventListener('click', async () => {
  // TODO: fetch('/api/booking/{id}/approve', { method:'POST' })
  closeAll();
  alert('อนุมัติคำร้องเรียบร้อย (ตัวอย่าง)');
});

rejectConfirmBtn.addEventListener('click', async () => {
  const reason = rejectReason.value.trim();
  if (!reason) {
    rejectReason.classList.add('is-invalid');
    rejectReason.focus();
    return;
  }
  // TODO: fetch('/api/booking/{id}/reject', { method:'POST', body: JSON.stringify({ reason }) })
  closeAll();
  alert('ตีกลับคำร้องแล้ว (ตัวอย่าง)\\nเหตุผล: ' + reason);
});

