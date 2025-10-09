const dpToggle = document.getElementById('dpToggle');
const dpPanel = document.getElementById('dpPanel');
const calTitle = document.getElementById('calTitle');
const calGrid = document.getElementById('calGrid');
const dpLabel = document.getElementById('dpLabel');

// Elements for navigation
const mainHeader = document.getElementById('main-header');
const tabsContainer = document.getElementById('tabs-container');
const breadcrumb = document.getElementById('breadcrumb');
const breadcrumbHome = document.getElementById('breadcrumb-home');
const breadcrumbCurrent = document.getElementById('breadcrumb-current');
const allRooms = document.querySelectorAll('.room-card');

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

// --- Page Navigation Logic ---

function showCategoryView(categoryName) {
  // Hide main header and tabs
  mainHeader.classList.add('hidden');
  tabsContainer.classList.add('hidden');

  // Show breadcrumb and set current category
  breadcrumbCurrent.textContent = categoryName;
  breadcrumb.classList.remove('hidden');

  // Filter rooms based on categoryName
  allRooms.forEach(room => {
    if (room.dataset.category === categoryName) {
      room.classList.remove('hidden');
    } else {
      room.classList.add('hidden');
    }
  });
}

function showHomeView() {
  // Show main header and tabs
  mainHeader.classList.remove('hidden');
  tabsContainer.classList.remove('hidden');

  // Hide breadcrumb
  breadcrumb.classList.add('hidden');

  // Show all rooms
  allRooms.forEach(room => {
    room.classList.remove('hidden');
  });
}

// Event listener for tabs
tabsContainer.addEventListener('click', (e) => {
  const clickedTab = e.target.closest('.tab');
  if (clickedTab) {
    // Update active tab
    tabsContainer.querySelector('.tab.active').classList.remove('active');
    clickedTab.classList.add('active');

    const categoryName = clickedTab.dataset.categoryName;
    showCategoryView(categoryName);
  }
});

// Event listener for breadcrumb home link
breadcrumbHome.addEventListener('click', (e) => {
  e.preventDefault(); // Prevent page reload
  showHomeView();
});