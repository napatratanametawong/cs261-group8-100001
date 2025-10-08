const dpToggle = document.getElementById('dpToggle');
const dpPanel = document.getElementById('dpPanel');
const calTitle = document.getElementById('calTitle');
const calGrid = document.getElementById('calGrid');
const dpLabel = document.getElementById('dpLabel');

let currentDate = new Date();

function renderCalendar(date) {
  calGrid.innerHTML = '';
  const year = date.getFullYear();
  const month = date.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const lastDate = new Date(year, month + 1, 0).getDate();

  calTitle.textContent = `${date.toLocaleString('th-TH', { month: 'long' })} ${year}`;

  // leading blanks
  for (let i = 0; i < firstDay; i++) {
    const empty = document.createElement('div');
    calGrid.appendChild(empty);
  }

  for (let day = 1; day <= lastDate; day++) {
    const cell = document.createElement('div');
    cell.textContent = day;
    cell.addEventListener('click', () => {
      const d = new Date(year, month, day);
      dpLabel.textContent = d.toLocaleDateString('th-TH');
      dpPanel.classList.remove('show');
    });
    // highlight today
    const today = new Date();
    if (day === today.getDate() && month === today.getMonth() && year === today.getFullYear()) {
      cell.style.fontWeight = '700';
      cell.style.background = '#f0f4ff';
      cell.style.borderRadius = '6px';
    }
    calGrid.appendChild(cell);
  }
}

// toggle panel
document.addEventListener('click', e => {
  if (e.target.closest('#dpToggle')) {
    dpPanel.classList.toggle('show');
  } else if (!e.target.closest('#dpPanel')) {
    dpPanel.classList.remove('show');
  }
});

// month navigation
document.querySelectorAll('.cal-nav').forEach(btn => {
  btn.addEventListener('click', () => {
    const dir = parseInt(btn.getAttribute('data-dir'));
    currentDate.setMonth(currentDate.getMonth() + dir);
    renderCalendar(currentDate);
  });
});

renderCalendar(currentDate);

// simple example: click book button
document.addEventListener('click', (e) => {
  if (e.target.closest('.book-btn')) {
    alert('เริ่มกระบวนการจอง — ฟังก์ชันนี้ยังไม่ได้เชื่อมต่อกับ backend');
  }
});