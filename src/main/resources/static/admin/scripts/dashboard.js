// adminDetails.js
// Script สำหรับจัดการการขยาย/ปิดรายละเอียดของแต่ละรายการ (accordion)
// วิธีใช้งาน: วางไฟล์นี้ไว้ในโฟลเดอร์ js/ แล้ว <script src="js/adminDetails.js"></script> ก่อนปิด </body>

(function () {
  'use strict';

  // คอนฟิก (ถ้าต้องการเปลี่ยนชื่อ class ให้แก้ที่นี่)
  const SELECTORS = {
    listContainer: '.list',        // container ของรายการทั้งหมด
    listItem: '.list-item',        // แต่ละรายการ
    toggleBtn: '.chev',            // ปุ่มลูกศร toggle
    details: '.details',           // panel รายละเอียด
    acceptBtn: '.btn-accept',      // ปุ่มอนุมัติ
    rejectBtn: '.btn-reject'       // ปุ่มตีกลับ
  };

  // State
  const requestDataByTab = {}; // เก็บข้อมูลคำร้องที่สร้างขึ้นสำหรับแต่ละแท็บ

  // เปิด/ปิดรายการ (li คือ element .list-item)
  function openItem(li) {
    if (!li) return;
    // ปิดอันอื่นก่อน (accordion behavior)
    closeAllExcept(li);

    li.classList.add('open');

    const btn = li.querySelector(SELECTORS.toggleBtn);
    if (btn) btn.setAttribute('aria-expanded', 'true');

    const det = li.querySelector(SELECTORS.details);
    if (det) det.setAttribute('aria-hidden', 'false');

    // หมุน/transition handled by css .list-item.open .chev
  }

  function closeItem(li) {
    if (!li) return;

    li.classList.remove('open');

    const btn = li.querySelector(SELECTORS.toggleBtn);
    if (btn) btn.setAttribute('aria-expanded', 'false');

    const det = li.querySelector(SELECTORS.details);
    if (det) det.setAttribute('aria-hidden', 'true');
  }

  function toggleItem(li) {
    if (!li) return;
    if (li.classList.contains('open')) {
      closeItem(li);
    } else {
      openItem(li);
      // เลื่อนเพื่อให้รายการอยู่ในมุมมอง หากต้องการ
      // ใช้ setTimeout เล็กน้อยเพื่อให้ transition/paint เกิดก่อน scroll
      setTimeout(() => {
        li.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }, 80);
    }
  }

  function closeAllExcept(exceptLi) {
    document.querySelectorAll(SELECTORS.listItem + '.open').forEach(li => {
      if (li !== exceptLi) {
        closeItem(li);
      }
    });
  }

  // Delegate click events from the list container
  function initDelegation() {
    const list = document.querySelector(SELECTORS.listContainer);
    if (!list) return;

    // click delegation for toggle buttons
    list.addEventListener('click', function (ev) {
      const toggle = ev.target.closest(SELECTORS.toggleBtn);
      if (toggle && list.contains(toggle)) {
        const li = toggle.closest(SELECTORS.listItem);
        if (!li) return;
        // Prevent accidental clicking inside interactive children
        ev.preventDefault();
        toggleItem(li);
      }

      // Accept/Reject buttons (action handlers)
      const accept = ev.target.closest(SELECTORS.acceptBtn);
      if (accept && list.contains(accept)) {
        const li = accept.closest(SELECTORS.listItem);
        if (!li) return;
        handleAccept(li, accept);
      }

      const reject = ev.target.closest(SELECTORS.rejectBtn);
      if (reject && list.contains(reject)) {
        const li = reject.closest(SELECTORS.listItem);
        if (!li) return;
        handleReject(li, reject);
      }
    });

    // keyboard accessibility: Enter / Space on toggle buttons
    list.addEventListener('keydown', function (ev) {
      const toggle = ev.target.closest(SELECTORS.toggleBtn);
      if (!toggle) return;
      if (ev.key === 'Enter' || ev.key === ' ') {
        ev.preventDefault();
        const li = toggle.closest(SELECTORS.listItem);
        toggleItem(li);
      }
    });
  }

  // ตัวอย่าง handler สำหรับอนุมัติ (คุณสามารถแก้ให้เรียก API ได้ที่นี่)
  function handleAccept(li, btn) {
    // สามารถอ่าน data จาก li (เช่น data-id) เพื่อส่งไปยัง API
    const requestId = li.dataset.requestId || null;

    // ตัวอย่าง UI feedback: ปิด panel และแสดงข้อความ (เปลี่ยนเป็นเรียก API จริงได้)
    // closeItem(li); // ไม่ต้องปิดทันที

    // ถ้าต้องการ: แสดง spinner / เปลี่ยนปุ่มเป็นกำลังทำงาน ฯลฯ
    // ตัวอย่าง: dispatch custom event เพื่อให้ระบบอื่นๆ ฟังได้
    const ev = new CustomEvent('request:accepted', { detail: { requestId, source: li } });
    document.dispatchEvent(ev);

    // ตัวอย่าง placeholder: alert (เอาออกใน production)
    // alert('อนุมัติคำร้อง ' + (requestId || '(no id)'));
    // ----- ส่วนนี้เปลี่ยนเป็น fetch() เรียก API จริงได้ -----
  }

  function handleReject(li, btn) {
    const requestId = li.dataset.requestId || null;
    // closeItem(li); // ไม่ต้องปิดทันที
    const ev = new CustomEvent('request:rejected', { detail: { requestId, source: li } });
    document.dispatchEvent(ev);
    // alert('ตีกลับคำร้อง ' + (requestId || '(no id)'));
    // ----- เปลี่ยนเป็นการเรียก API / modal confirm ได้ -----
  }

  // ===== Data Generation & Rendering (for demo) =====

  // ฟังก์ชันสร้างข้อมูลคำร้องสุ่ม 1 รายการ
  function generateRandomRequest() {
    const names = ['สมชาย เข็มกลัด', 'สมหญิง ยิ่งสุข', 'ประยุทธ์ จันทร์ดี', 'ทักษิณ ชินวัตร', 'ยิ่งลักษณ์ ชินวัตร', 'พิธา ลิ้มเจริญรัตน์'];
    const rooms = ['LC2-201', 'LC2-209', 'LC2-304', 'LC2-405', 'ห้องประชุม 1'];
    const times = ['09:00-11:00', '11:00-13:00', '13:30-15:30', '15:30-18:00'];

    const date = new Date(Date.now() + Math.random() * 30 * 24 * 60 * 60 * 1000);
    const formattedDate = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear() + 543}`;

    return {
      id: Math.floor(100000000 + Math.random() * 900000000),
      date: formattedDate,
      name: names[Math.floor(Math.random() * names.length)],
      room: rooms[Math.floor(Math.random() * rooms.length)],
      time: times[Math.floor(Math.random() * times.length)],
    };
  }

  // ฟังก์ชันสร้าง HTML สำหรับ 1 รายการ
  function createRequestItemHTML(request, tabKey) {
    // แสดงปุ่ม "อนุมัติ" และ "ตีกลับ" เฉพาะในแท็บ "คำร้องใหม่"
    const actionButtonsHTML = tabKey === 'new' ? `
      <div class="action-row">
        <button class="btn btn-accept">อนุมัติคำร้อง</button>
        <button class="btn btn-reject">ตีกลับคำร้อง</button>
      </div>
    ` : '';

    return `
      <div class="list-item" data-request-id="${request.id}">
        <div class="item-grid">
          <div class="item-cell">${request.date}</div>
          <div class="item-cell">${request.id}</div>
          <div class="item-cell">${request.name}</div>
          <div class="item-cell">${request.room}</div>
          <div class="item-cell item-right">
            <span class="time">${request.time}</span>
            <button class="chev" aria-expanded="false" aria-label="รายละเอียด">
              <img src="../resource/chevron-down.svg" alt="Toggle Details" />
            </button>
          </div>
        </div>
        <div class="details" role="region" aria-hidden="true">
          <div class="details-inner">
            <div class="details-left">
              <h3 class="details-title">รายละเอียดการจอง</h3>
              <div class="detail-row-group">
                <div class="detail-row"><strong>ชื่อ-นามสกุล</strong><div>${request.name}</div></div>
                <div class="detail-row"><strong>อีเมล</strong><div>xxxx@dome.tu.ac.th</div></div>
              </div>
              <div class="detail-row-group">
                <div class="detail-row"><strong>วันที่ต้องการใช้ห้อง</strong><div>${request.date}</div></div>
                <div class="detail-row"><strong>ช่วงเวลา</strong><div>${request.time}</div></div>
              </div>
              <div class="detail-row-group">
                <div class="detail-row"><strong>ประเภทห้อง</strong><div>ห้องเรียน</div></div>
                <div class="detail-row"><strong>ห้อง</strong><div>${request.room}</div></div>
              </div>
            </div>
            <div class="details-right">
              <h3 class="details-title accent">กรุณากรอกข้อมูลเพิ่มเติม</h3>
              <div class="detail-row-group">
                <div class="detail-row"><strong>กลุ่มผู้ยื่นคำร้อง<span class="required-star">*</span></strong><div>ชมรม xxxx</div></div>
                <div class="detail-row"><strong>เบอร์โทรศัพท์<span class="required-star">*</span></strong><div>xxx-xxx-xxxx</div></div>
              </div>
              <div class="detail-row"><strong>จุดประสงค์การขอใช้ห้อง<span class="required-star">*</span></strong><div class="purpose">เพื่อการเรียนการสอน</div></div>
              <div class="file-upload">
                <label for="doc-${request.id}">อัปโหลดเอกสารเพิ่มเติม</label>
                <input id="doc-${request.id}" type="text" value="เอกสารแนบ.pdf" readonly />
              </div>
              ${actionButtonsHTML}
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // ฟังก์ชันแสดงผลรายการคำร้อง
  function renderRequests(container, requests, tabKey) {
    if (requests && requests.length > 0) {
      container.innerHTML = requests.map(request => createRequestItemHTML(request, tabKey)).join('');
    } else {
      container.innerHTML = `<div class="list-item" role="status"><div class="item-grid" style="display:block; text-align:center;">ยังไม่มีคำร้องในสถานะนี้</div></div>`;
    }
  }

  // ===== Tab Handling =====
  function handleTabSwitch(tab) {
    const listContainer = document.querySelector(SELECTORS.listContainer);
    if (!listContainer) return;

    const tabKey = tab.dataset.tabKey;

    // แสดงสถานะกำลังโหลด
    listContainer.innerHTML = `<div class="list-item" role="status"><div class="item-grid" style="display:block; text-align:center;">กำลังโหลดข้อมูล...</div></div>`;

    // --- ส่วนนี้จะถูกแทนที่ด้วยการเรียก API จริง ---
    // จำลองการเรียก API และแสดงผลลัพธ์
    setTimeout(() => {
      const data = requestDataByTab[tabKey] || [];
      renderRequests(listContainer, data, tabKey);
    }, 300); // หน่วงเวลา 0.3 วินาทีเพื่อจำลองการโหลด
  }

  function initTabs() {
    const tabsContainer = document.querySelector('.tabs');
    if (!tabsContainer) return;

    tabsContainer.addEventListener('click', (ev) => {
      const tab = ev.target.closest('.tab');
      if (!tab || tab.classList.contains('tab--active')) return;

      // 1. Update visual state
      tabsContainer.querySelectorAll('.tab').forEach(t => {
        t.classList.remove('tab--active');
        t.setAttribute('aria-selected', 'false');
      });
      tab.classList.add('tab--active');
      tab.setAttribute('aria-selected', 'true');

      // 2. Handle data fetching/display
      handleTabSwitch(tab);
    });
  }


  // Public init
  function init() {
    initDelegation();
    initTabs();
    
    // --- สร้างข้อมูลตัวอย่างเมื่อโหลดหน้า ---
    const TABS = ['new', 'pending', 'completed'];
    const counts = {
      new: Math.floor(Math.random() * 5) + 5, // 5-9
      pending: Math.floor(Math.random() * 3) + 2, // 2-4
      completed: Math.floor(Math.random() * 8) + 8, // 8-15
    };

    TABS.forEach(tabKey => {
      requestDataByTab[tabKey] = Array.from({ length: counts[tabKey] }, generateRandomRequest);
    });

    // แสดงผลแท็บแรก (คำร้องใหม่)
    const listContainer = document.querySelector(SELECTORS.listContainer);
    if (listContainer) {
      renderRequests(listContainer, requestDataByTab['new'], 'new');
    }
  }

  // เรียก init เมื่อ DOM โหลดแล้ว
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  // Export (ไม่จำเป็น แต่เปิดช่องให้เรียกจาก console ได้)
  window.AdminDetails = {
    openItem,
    closeItem,
    toggleItem,
    closeAllExcept
  };

})();
