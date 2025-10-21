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
// === Booking API integration ===
(function(){
  const roomsContainer = document.querySelector('.rooms');
  const API_BASE = location.origin;
  const SLOT_LABEL = {
    'S0800_0930': '08:00-09:30',
    'S0930_1100': '09:30-11:00',
    'S1100_1230': '11:00-12:30',
    'S1330_1500': '13:30-15:00',
    'S1500_1630': '15:00-16:30',
    'S1630_1800': '16:30-18:00'
  };
  function toISODate(d){
    var y=d.getFullYear();
    var m=('0'+(d.getMonth()+1)).slice(-2);
    var day=('0'+d.getDate()).slice(-2);
    return y+'-'+m+'-'+day;
  }
  function renderLoading(message){
    if(!roomsContainer) return;
    var msg = message || 'Loading room status...';
    roomsContainer.innerHTML = '<div class="room-card"><div class="info"><div class="title">'+msg+'</div></div></div>';
  }
  function renderError(message){
    if(!roomsContainer) return;
    var msg = message || 'Failed to load room status';
    roomsContainer.innerHTML = '<div class="room-card"><div class="info"><div class="title" style="color:red;">'+msg+'</div></div></div>';
  }
  function renderRooms(data){
    if(!roomsContainer) return;
    var rooms = new Map();
    (data||[]).forEach(function(row){
      var code = row.roomCode || row.roomcode || row.CODE || '';
      var name = row.roomName || row.roomname || row.ROOM_NAME || '';
      var slot = row.slotCode || row.slotcode || row.SLOT_CODE || '';
      var status = String(row.roomStatus || row.roomstatus || row.ROOM_STATUS || '').toLowerCase();
      var key = code+'||'+name;
      if(!rooms.has(key)) rooms.set(key,{code:code,name:name,slots:{}});
      rooms.get(key).slots[slot] = status; // 'booked' or 'available'
    });
    var frag = document.createDocumentFragment();
    rooms.forEach(function(v){
      var card=document.createElement('div'); card.className='room-card';
      var media=document.createElement('div'); media.className='media'; media.textContent='';
      var info=document.createElement('div'); info.className='info';
      var title=document.createElement('div'); title.className='title'; title.textContent=(v.code+'  '+v.name).trim();
      var meta=document.createElement('div'); meta.className='meta'; meta.innerHTML='<span class="icon"></span><span></span>';
      var slotsDiv=document.createElement('div'); slotsDiv.className='slots';
      Object.keys(SLOT_LABEL).forEach(function(sc){
        var span=document.createElement('span');
        var stat=(v.slots[sc]||'').toLowerCase();
        var busy=(stat==='booked');
        span.className='slot '+(busy?'busy':'free');
        span.textContent=SLOT_LABEL[sc];
        slotsDiv.appendChild(span);
      });
      info.appendChild(title); info.appendChild(meta); info.appendChild(slotsDiv);
      var cta=document.createElement('div'); cta.className='cta';
      var btn=document.createElement('button'); btn.className='btn-book book-btn'; btn.textContent='Book'; cta.appendChild(btn);
      card.appendChild(media); card.appendChild(info); card.appendChild(cta);
      frag.appendChild(card);
    });
    roomsContainer.innerHTML='';
    roomsContainer.appendChild(frag);
  }
  async function fetchAndRender(dateIso){
    renderLoading();
    try{
      var url = API_BASE + '/api/rooms/status?date=' + encodeURIComponent(dateIso);
      var res = await fetch(url, { method:'GET', credentials:'include', headers:{'Accept':'application/json'} });
      if(!res.ok) throw new Error('HTTP '+res.status);
      var data = await res.json();
      renderRooms(data);
    }catch(e){ console.warn('fetchAndRender failed', e); renderError('????????????????????????'); }
  }
  // hook: clicking a day triggers fetch (in addition to existing handler)
  var calGridEl = document.getElementById('calGrid');
  if(calGridEl){
    calGridEl.addEventListener('click', function(e){
      var t=e.target; if(!t || !t.textContent) return;
      var day=parseInt(t.textContent,10); if(!day) return;
      // relies on global currentDate from original script
      try {
        var d = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
        fetchAndRender(toISODate(d));
      } catch(_) {}
    });
  }
  // enhance book buttons if original handler misses them
  document.addEventListener('click', function(e){
    if(e.target && (e.target.closest('.btn-book'))){
      alert('Booking flow not wired yet');
    }
  });
  // initial load
  if(typeof currentDate==='undefined'){ try{ currentDate=new Date(); }catch(_){}}
  try{ document.getElementById('dpLabel').textContent = currentDate.toLocaleDateString('th-TH'); }catch(_){ }
  fetchAndRender(toISODate(currentDate));
})();