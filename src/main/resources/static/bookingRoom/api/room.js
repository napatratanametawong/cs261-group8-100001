// rooms.js — API fetch + render + tabs + breadcrumb
(function () {
  const roomsContainer = document.querySelector('.rooms');

  // ===== context path candidates (/first-seg, /) =====
  const firstSeg = location.pathname.split('/').filter(Boolean)[0] || '';
  const ctxFromPath = firstSeg ? `/${firstSeg}` : '';
  const API_BASES = Array.from(new Set([ctxFromPath, '']));

  const SLOT_LABEL = {
    S0800_0930: '08:00 - 09:30',
    S0930_1100: '09:30 - 11:00',
    S1100_1230: '11:00 - 12:30',
    S1330_1500: '13:30 - 15:00',
    S1500_1630: '15:00 - 16:30',
    S1630_1800: '16:30 - 18:00',
  };

  // ===== UI refs (ต้องมี id เหล่านี้ใน HTML) =====
  const mainHeader    = document.getElementById('main-header');      // hero/แบนเนอร์
  const tabsContainer = document.getElementById('tabs-container');   // แถบแท็บ
  const breadcrumb    = document.getElementById('breadcrumb');       // กล่อง breadcrumb (เริ่มต้น .hidden)
  const bcHome        = document.getElementById('breadcrumb-home');  // ลิงก์ "หน้าแรก"
  const bcCurrent     = document.getElementById('breadcrumb-current');

  // ===== state =====
  let selectedCategory = '';   // '' = แสดงทั้งหมด
  let userChoseTab = false;    // ยังไม่กดแท็บ = ไม่กรอง

  // ===== helpers =====
  const toISO = (d) => {
    const y = d.getFullYear(), m = ('0' + (d.getMonth() + 1)).slice(-2), dd = ('0' + d.getDate()).slice(-2);
    return `${y}-${m}-${dd}`;
  };

  function resolveCategory(roomType) {
    const t = (roomType || '').toLowerCase();
    if (t.includes('computer') || t.includes('lab') || t.includes('ปฏิบัติการ')) return 'ห้องปฏิบัติการทางคอมพิวเตอร์';
    if (t.includes('meeting')  || t.includes('conference') || t.includes('ประชุม')) return 'ห้องประชุม';
    if (t.includes('lecture')  || t.includes('class') || t.includes('เรียน') || t.includes('ห้องเรียน')) return 'ห้องเรียน';
    return roomType || 'ห้องเรียน';
  }

  function renderLoading(msg) {
    if (!roomsContainer) return;
    roomsContainer.innerHTML =
      `<div class="room-card" role="status" aria-live="polite"><div class="info"><div class="title">${msg || 'กำลังโหลดสถานะห้อง...'}</div></div></div>`;
  }
  function renderError(msg) {
    if (!roomsContainer) return;
    roomsContainer.innerHTML = `<p class="no-rooms-message" role="status" aria-live="polite">${msg || 'ไม่พบข้อมูลห้อง'}</p>`;
  }
  function safeParseArr(x){ if(!x || typeof x!=='string') return []; try{ const a=JSON.parse(x); return Array.isArray(a)?a:[] }catch{ return [] } }
  function normalizeStatus(raw){
    if(!Array.isArray(raw)) return [];
    if(raw.length && typeof raw[0]?.slots==='object'){
      return raw.map(it=>({
        code: it.code || it.roomCode || '',
        roomName: it.roomName || '',
        roomType: it.roomType || '',
        minCapacity: it.minCapacity ?? null,
        maxCapacity: it.maxCapacity ?? null,
        features: Array.isArray(it.features) ? it.features : safeParseArr(it.featuresJson),
        slots: it.slots || {},
        generatedAt: it.generatedAt || null,
      }));
    }
    const map=new Map();
    raw.forEach(row=>{
      const code=(row.roomCode||row.CODE||'').trim();
      const name=row.roomName||row.ROOM_NAME||'';
      const slot=(row.slotCode||row.SLOT_CODE||'').trim();
      const status=String(row.roomStatus||row.ROOM_STATUS||'').trim();
      if(!code) return;
      if(!map.has(code)){
        map.set(code,{code,roomName:name,roomType:'',minCapacity:null,maxCapacity:null,features:[],slots:{},generatedAt:null});
      }
      if(slot) map.get(code).slots[slot]=status;
    });
    return [...map.values()];
  }
  async function fetchJSON(url,opt){
    const res=await fetch(url,opt);
    const ct=res.headers.get('content-type')||'';
    if(!res.ok) throw new Error(`HTTP ${res.status}`);
    if(!ct.includes('application/json')) throw new Error('Non-JSON');
    return res.json();
  }
  async function tryBases(pathAndQuery,opt){
    let lastErr;
    for(const base of API_BASES){
      try{ return await fetchJSON(`${base}${pathAndQuery}`,opt); }catch(e){ lastErr=e; }
    }
    throw lastErr || new Error('Fetch failed');
  }

  // ===== filtering + view toggles =====
  function applyCategoryFilter(){
    document.querySelectorAll('.room-card').forEach(card=>{
      const cat = (card.dataset.category || '').trim();
      card.classList.toggle('hidden', !!selectedCategory && cat !== selectedCategory);
    });
  }
  function showCategoryView(name){
    selectedCategory = name || '';
    mainHeader?.classList.add('hidden');
    tabsContainer?.classList.add('hidden');
    breadcrumb?.classList.remove('hidden');
    if (bcCurrent) bcCurrent.textContent = selectedCategory || 'ทั้งหมด';
    applyCategoryFilter();
  }
  function showHomeView(){
    selectedCategory = '';
    mainHeader?.classList.remove('hidden');
    tabsContainer?.classList.remove('hidden');
    breadcrumb?.classList.add('hidden');
    // ล้างสถานะแท็บที่ active
    tabsContainer?.querySelectorAll('.tab').forEach(t=>{
      t.classList.remove('active');
      t.setAttribute('aria-selected','false');
    });
    applyCategoryFilter();
    userChoseTab = false;
  }

  // ===== render cards =====
  function render(list){
    if(!roomsContainer) return;
    if(!list || !list.length){ renderError('ไม่พบข้อมูลห้อง'); return; }

    const frag=document.createDocumentFragment();
    list.forEach(v=>{
      const card=document.createElement('div');
      card.className='room-card';
      const category = resolveCategory(v.roomType);
      card.dataset.category = category;

      const info=document.createElement('div'); info.className='info';
      const title=document.createElement('div'); title.className='title';
      title.textContent = `${v.code || ''}  ${v.roomName || ''}`.trim();

      const meta=document.createElement('div'); meta.className='meta';
      meta.innerHTML = `
        <span class="type"> ประเภทห้อง: ${category}</span>
        <span class="capacity"><img src="/resource/user.svg" class="cap_icon" alt=""> ${v.minCapacity ?? '-'} – ${v.maxCapacity ?? '-'} คน</span>`;

      const ul=document.createElement('ul'); ul.className='features';
      (v.features||[]).forEach(f=>{
        const li=document.createElement('li');
        const lower = (''+f).trim().toLowerCase();
        if(lower==='projector'){
          li.innerHTML = `<img src="/resource/projector.svg" alt="Projector" style="width:18px;height:18px;vertical-align:middle;margin-right:4px;">Projector`;
        }else if(lower==='whiteboard'){
          li.innerHTML = `<img src="/resource/whiteboard.svg" alt="Whiteboard" style="width:18px;height:18px;vertical-align:middle;margin-right:4px;">Whiteboard`;
        }else if(lower==='computer'){
          li.innerHTML = `<img src="/resource/computer.svg" alt="Computer" style="width:18px;height:18px;vertical-align:middle;margin-right:4px;">Computer`;
        }else{
          li.textContent=f;
        }
        ul.appendChild(li);
      });

      const divider=document.createElement('div'); divider.className='divider';

      const slots=document.createElement('div'); slots.className='slots';
      Object.keys(SLOT_LABEL).forEach(sc=>{
        const span=document.createElement('span');
        const raw=v.slots?.[sc]?String(v.slots[sc]).toLowerCase():'available';
        const busy = raw==='booked'||raw==='reserved'||raw==='busy';
        span.className = 'slot ' + (busy?'busy':'free');
        span.textContent = SLOT_LABEL[sc];
        span.dataset.slotCode = sc;
        slots.appendChild(span);
      });

      const cta=document.createElement('div'); cta.className='cta';
      const btn=document.createElement('button'); btn.className='btn-book book-btn'; btn.textContent='Book';
      // expose room meta for downstream handlers and disable by default
      btn.dataset.roomCode = (v.code || '').trim();
      btn.dataset.roomName = (v.roomName || '').trim();
      btn.disabled = true;
      btn.setAttribute('aria-disabled','true');
      cta.appendChild(btn);

      info.appendChild(title);
      info.appendChild(meta);
      if((v.features||[]).length) info.appendChild(ul);
      info.appendChild(divider);
      info.appendChild(slots);

      card.appendChild(info);
      card.appendChild(cta);
      frag.appendChild(card);
    });

    roomsContainer.innerHTML='';
    roomsContainer.appendChild(frag);

    // ให้ timeSelect/booking รู้ว่าพร้อมแล้ว
    window.dispatchEvent(new CustomEvent('rooms:rendered'));

    // ถ้าเคยเลือกแท็บไว้ ให้คงการกรองเดิม
    if (userChoseTab) applyCategoryFilter();
  }

  // ===== load by date =====
  async function load(dateISO){
    renderLoading();
    try{
      const raw = await tryBases(`/api/rooms/status?date=${encodeURIComponent(dateISO)}`, {
        credentials:'include',
        headers:{ Accept:'application/json' }
      });
      render(normalizeStatus(raw));
    }catch(e){
      renderError('ไม่พบข้อมูลห้อง');
    }
  }

  // ===== tabs click (ตัวเดียวจบ) =====
  tabsContainer?.addEventListener('click', (e)=>{
    const tab = e.target.closest('.tab');
    if(!tab) return;
    userChoseTab = true;

    // active state
    tabsContainer.querySelectorAll('.tab').forEach(t=>{
      t.classList.remove('active');
      t.setAttribute('aria-selected','false');
    });
    tab.classList.add('active');
    tab.setAttribute('aria-selected','true');

    // เข้าโหมดหมวด + กรอง
    showCategoryView((tab.dataset.categoryName || '').trim());
  });

  // breadcrumb home
  bcHome?.addEventListener('click', (e)=>{
    e.preventDefault();
    showHomeView();
  });

  // เมื่อมีการเปลี่ยนวันที่จาก calendar
  window.addEventListener('calendar:date-change', (ev)=>{
    const { iso } = ev.detail || {};
    if (iso) load(iso);
  });

  // initial: today, โชว์ทั้งหมด (ยังไม่กรอง/ไม่ซ่อนเฮดเดอร์)
  const firstISO =
    (window.CalendarAPI && typeof CalendarAPI.getSelectedISO === 'function'
      ? CalendarAPI.getSelectedISO()
      : toISO(new Date()));
  load(firstISO);

  // expose (optional)
  window.RoomsAPI = {
    reloadFor(date){ const d = date instanceof Date ? date : new Date(date); return load(toISO(d)); }
  };
})();
