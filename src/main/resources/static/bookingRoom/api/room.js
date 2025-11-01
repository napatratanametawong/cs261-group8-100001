// ===== Datepicker, Tabs, Breadcrumb & Booking Integration =====

// DOM refs
const dpToggle = document.getElementById('dpToggle');
const dpPanel = document.getElementById('dpPanel');
const calTitle = document.getElementById('calTitle');
const calGrid = document.getElementById('calGrid');
const dpLabel = document.getElementById('dpLabel');

const mainHeader = document.getElementById('main-header');
const tabsContainer = document.getElementById('tabs-container');
const breadcrumb = document.getElementById('breadcrumb');
const breadcrumbHome = document.getElementById('breadcrumb-home');
const breadcrumbCurrent = document.getElementById('breadcrumb-current');

// ===== Calendar =====
// ใช้ currentDate 
let currentDate = new Date();
let selectedDate = new Date(); 

//ทียบว่าเป็นวันเดียวกันหรือไม่ (ตัดเวลาออก)
function isSameDay(a, b) {
  if (!a || !b) return false;
  return a.getFullYear() === b.getFullYear()
      && a.getMonth() === b.getMonth()
      && a.getDate() === b.getDate();
}

// helper: label ไทย
function toThaiDateString(d) {
  return d.toLocaleDateString('th-TH');
}

function renderCalendar(date) {
  if (!calGrid || !calTitle) return;

  calGrid.innerHTML = '';
  const year = date.getFullYear();
  const month = date.getMonth();
  const firstDay = new Date(year, month, 1).getDay();     // 0=อา ... 6=ส
  const lastDate  = new Date(year, month + 1, 0).getDate();

  calTitle.textContent = `${date.toLocaleString('th-TH', { month: 'long' })} ${year}`;

  // ช่องว่างก่อนวันที่ 1
  for (let i = 0; i < firstDay; i++) {
    const empty = document.createElement('div');
    empty.className = 'blank';
    calGrid.appendChild(empty);
  }

  const today = new Date(); 

  for (let day = 1; day <= lastDate; day++) {
    const cell = document.createElement('div');
    const d = new Date(year, month, day);
    cell.textContent = day;

    // ไฮไลต์ "วันนี้"
    if (isSameDay(d, today)) {
      cell.classList.add('is-today');
    }

    // ไฮไลต์ "วันที่เลือก"
    if (isSameDay(d, selectedDate)) {
      cell.classList.add('is-selected');
    }

    cell.addEventListener('click', () => {
      // อัปเดตวันที่ที่เลือก
      selectedDate = d;
      // อัปเดต label
      if (dpLabel) dpLabel.textContent = toThaiDateString(selectedDate);
      // ปิดปฏิทิน
      dpPanel?.classList.remove('show');
      dpToggle?.setAttribute('aria-expanded', 'false');

      // re-render เพื่ออัปเดตคลาส is-selected
      renderCalendar(currentDate);

      // โหลดข้อมูลของวันที่เลือก (เรียกผ่านฟังก์ชันที่ IIFE เปิดไว้)
      if (window.fetchRoomsForDate) {
        window.fetchRoomsForDate(selectedDate);
      }
    });

    calGrid.appendChild(cell);
  }
}

// เปิด/ปิดปฏิทิน 
document.addEventListener('click', (e) => {
  if (e.target.closest('#dpToggle')) {
    const open = !dpPanel?.classList.contains('show');
    dpPanel?.classList.toggle('show');
    dpToggle?.setAttribute('aria-expanded', open ? 'true' : 'false');
  } else if (!e.target.closest('#dpPanel')) {
    dpPanel?.classList.remove('show');
    dpToggle?.setAttribute('aria-expanded', 'false');
  }
});

// เปลี่ยนเดือนซ้าย/ขวา 
document.querySelectorAll('.cal-nav').forEach((btn) => {
  btn.addEventListener('click', () => {
    const dir = parseInt(btn.getAttribute('data-dir'), 10) || 0;
    currentDate.setMonth(currentDate.getMonth() + dir);
    renderCalendar(currentDate);
  });
});

if (dpLabel) dpLabel.textContent = toThaiDateString(selectedDate);
renderCalendar(currentDate);

// ===== Navigation: Tabs & Breadcrumb =====
function showCategoryView(categoryName) {
  mainHeader?.classList.add('hidden');
  tabsContainer?.classList.add('hidden');

  breadcrumbCurrent.textContent = categoryName;
  breadcrumb?.classList.remove('hidden');

  document.querySelectorAll('.room-card').forEach((card) => {
    card.classList.toggle('hidden', card.dataset.category !== categoryName);
  });
}

function showHomeView() {
  mainHeader?.classList.remove('hidden');
  tabsContainer?.classList.remove('hidden');
  breadcrumb?.classList.add('hidden');

  document.querySelectorAll('.room-card').forEach((card) => {
    card.classList.remove('hidden');
  });
}

if (tabsContainer) {
  tabsContainer.addEventListener('click', (e) => {
    const clickedTab = e.target.closest('.tab');
    if (!clickedTab) return;

    const active = tabsContainer.querySelector('.tab.active');
    if (active) active.classList.remove('active');
    clickedTab.classList.add('active');

    const categoryName = clickedTab.dataset.categoryName;
    showCategoryView(categoryName);
  });
}

breadcrumbHome?.addEventListener('click', (e) => {
  e.preventDefault();
  showHomeView();
});

// ===== Booking API integration =====
(function () {
  const roomsContainer = document.querySelector('.rooms');
  const API_BASE = location.origin;

  const SLOT_LABEL = {
    S0800_0930: '08:00-09:30',
    S0930_1100: '09:30-11:00',
    S1100_1230: '11:00-12:30',
    S1330_1500: '13:30-15:00',
    S1500_1630: '15:00-16:30',
    S1630_1800: '16:30-18:00',
  };

  const CATEGORY_MAP = {
    'Lecture Room': 'ห้องเรียน',
    'Meeting Room': 'ห้องประชุม',
    'Computer Lab': 'ห้องปฏิบัติการทางคอมพิวเตอร์',
  };

  function toISODate(d) {
    const y = d.getFullYear();
    const m = ('0' + (d.getMonth() + 1)).slice(-2);
    const day = ('0' + d.getDate()).slice(-2);
    return `${y}-${m}-${day}`;
  }

  function renderLoading(message) {
    if (!roomsContainer) return;
    const msg = message || 'กำลังโหลดสถานะห้อง...';
    roomsContainer.innerHTML =
      `<div class="room-card"><div class="info"><div class="title">${msg}</div></div></div>`;
  }

  function renderError(message) {
    if (!roomsContainer) return;
    const msg = message || 'ไม่สามารถโหลดข้อมูลห้องได้';
    roomsContainer.innerHTML =
      `<div class="room-card"><div class="info"><div class="title" style="color:#c0392b;">${msg}</div></div></div>`;
  }

  
  function safeParseJsonArray(json) {
    if (!json || typeof json !== 'string') return [];
    try { const arr = JSON.parse(json); return Array.isArray(arr) ? arr : []; }
    catch { return []; }
  }
  
  function normalizeGrouped(data) {
    if (!Array.isArray(data)) return [];
    if (data.length && typeof data[0]?.slots === 'object') {
      return data.map((item) => ({
        code: item.code || item.roomCode || '',
        roomName: item.roomName || '',
        roomType: item.roomType || '',
        minCapacity: item.minCapacity ?? null,
        maxCapacity: item.maxCapacity ?? null,
        features: Array.isArray(item.features) ? item.features : safeParseJsonArray(item.featuresJson),
        slots: item.slots || {},
        generatedAt: item.generatedAt || null,
      }));
    }
    const byCode = new Map();
    data.forEach((row) => {
      const code = (row.roomCode || row.CODE || '').trim();
      const name = row.roomName || row.ROOM_NAME || '';
      const slot = (row.slotCode || row.SLOT_CODE || '').trim();
      const status = String(row.roomStatus || row.ROOM_STATUS || '').trim();
      if (!code) return;
      if (!byCode.has(code)) {
        byCode.set(code, { code, roomName: name, roomType: '', minCapacity: null, maxCapacity: null, features: [], slots: {}, generatedAt: null });
      }
      if (slot) byCode.get(code).slots[slot] = status;
    });
    return Array.from(byCode.values());
  }

  function renderRoomsGrouped(list) {
    if (!roomsContainer) return;
    if (!list || !list.length) {
      roomsContainer.innerHTML = '<p class="no-rooms-message">ไม่พบข้อมูลห้อง</p>';
      return;
    }
    const frag = document.createDocumentFragment();
    list.forEach((v) => {
      const card = document.createElement('div');
      card.className = 'room-card';
      const category = CATEGORY_MAP[v.roomType] || v.roomType || 'ห้องเรียน';
      card.dataset.category = category;

      const info = document.createElement('div'); info.className = 'info';
      const title = document.createElement('div'); title.className = 'title';
      title.textContent = `${v.code || ''}  ${v.roomName || ''}`.trim();

      const meta = document.createElement('div'); meta.className = 'meta';
      meta.innerHTML = `
        <span class="type"> ประเภทห้อง: ${category}</span>
        <span class="capacity">
          <img src="/resource/user.svg" alt="icon capacity" class="cap_icon" />
          ${v.minCapacity ?? '-'} – ${v.maxCapacity ?? '-'} คน</span>
      `;

      const featureList = document.createElement('ul'); featureList.className = 'features';
      (v.features || []).forEach((f) => { const li = document.createElement('li'); li.textContent = f; featureList.appendChild(li); });

      const slotsDiv = document.createElement('div'); slotsDiv.className = 'slots';
      Object.keys(SLOT_LABEL).forEach((sc) => {
        const span = document.createElement('span');
        const raw = v.slots?.[sc] ? String(v.slots[sc]).toLowerCase() : 'available';
        const busy = raw === 'booked' || raw === 'reserved' || raw === 'busy';
        span.className = 'slot ' + (busy ? 'busy' : 'free');
        span.textContent = SLOT_LABEL[sc];
        slotsDiv.appendChild(span);
      });

      const cta = document.createElement('div'); cta.className = 'cta';
      const btn = document.createElement('button'); btn.className = 'btn-book book-btn'; btn.textContent = 'Book';
      cta.appendChild(btn);

      info.appendChild(title);
      info.appendChild(meta);
      if ((v.features || []).length) info.appendChild(featureList);
      info.appendChild(slotsDiv);

      card.appendChild(info);
      card.appendChild(cta);
      frag.appendChild(card);
    });
    roomsContainer.innerHTML = '';
    roomsContainer.appendChild(frag);
  }

  async function fetchAndRender(dateIso) {
    renderLoading();
    try {
      const url = `${API_BASE}/api/rooms/status?date=${encodeURIComponent(dateIso)}`;
      const res = await fetch(url, { method: 'GET', credentials: 'include', headers: { Accept: 'application/json' } });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      const raw = await res.json();
      const grouped = normalizeGrouped(raw);
      renderRoomsGrouped(grouped);
    } catch (e) {
      console.warn('fetchAndRender failed', e);
      renderError('ไม่พบข้อมูลห้อง');
    }
  }

  // initial load
  try {
    if (typeof currentDate === 'undefined') window.currentDate = new Date();
    if (dpLabel) dpLabel.textContent = toThaiDateString(selectedDate);
    fetchAndRender(toISODate(selectedDate));
  } catch (_) {}

  // เปิดจอง (placeholder)
  document.addEventListener('click', (e) => {
    if (e.target && e.target.closest('.btn-book')) {
      alert('เริ่มกระบวนการจอง — (ยังไม่ได้เชื่อมต่อ backend)');
    }
  });

  // expose ให้ calendar เรียกใช้
  window.fetchRoomsForDate = (d) => fetchAndRender(toISODate(d));
})();
