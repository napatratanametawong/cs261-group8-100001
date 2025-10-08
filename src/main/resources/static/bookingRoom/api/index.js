const dpToggle = document.getElementById('dpToggle');
const dpPanel = document.getElementById('dpPanel');
const calTitle = document.getElementById('calTitle');
const calGrid = document.getElementById('calGrid');

let currentDate = new Date();

function renderCalendar(date) {
  calGrid.innerHTML = '';
  const year = date.getFullYear();
  const month = date.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const lastDate = new Date(year, month + 1, 0).getDate();

  calTitle.textContent = `${date.toLocaleString('default', { month: 'long' })} ${year}`;

  for (let i = 0; i < firstDay; i++) {
    const empty = document.createElement('div');
    calGrid.appendChild(empty);
  }

  for (let day = 1; day <= lastDate; day++) {
    const cell = document.createElement('div');
    cell.textContent = day;
    calGrid.appendChild(cell);
  }
}

document.addEventListener('click', e => {
  if (e.target.closest('#dpToggle')) {
    dpPanel.classList.toggle('show');
  } else if (!e.target.closest('#dpPanel')) {
    dpPanel.classList.remove('show');
  }
});

document.querySelectorAll('.cal-nav').forEach(btn => {
  btn.addEventListener('click', () => {
    const dir = parseInt(btn.getAttribute('data-dir'));
    currentDate.setMonth(currentDate.getMonth() + dir);
    renderCalendar(currentDate);
  });
});

renderCalendar(currentDate);
