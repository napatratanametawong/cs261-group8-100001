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
  const allReservations = {
    new: [],
    pending: [],
    completed: []
  };

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

  // Handler for "Approve" button click
  async function handleAccept(li, btn) {
    const requestId = li.dataset.requestId || null;
    if (!requestId) return;

    // This will be handled by modals.js
    document.dispatchEvent(new CustomEvent('request:approve', { detail: { requestId, source: li } }));
  }

  // Handler for "Reject" button click
  function handleReject(li, btn) {
    const requestId = li.dataset.requestId || null;
    if (!requestId) return;

    // This will be handled by modals.js
    document.dispatchEvent(new CustomEvent('request:reject', { detail: { requestId, source: li } }));
  }

  // Function to call the approve API
  async function approveRequest(requestId) {
    try {
      const response = await fetch(`/api/staff/logs/${requestId}/reviewed`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          // Include Authorization header if needed, e.g., 'Authorization': `Bearer ${token}`
        },
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      document.dispatchEvent(new CustomEvent('ui:show-status', {
        detail: { title: 'สำเร็จ', message: 'อนุมัติคำร้องสำเร็จ', isError: false }
      }));
      fetchAndCategorizeReservations(); // Refresh data
    } catch (error) {
      console.error('Approve request failed:', error);
      document.dispatchEvent(new CustomEvent('ui:show-status', {
        detail: {
          title: 'เกิดข้อผิดพลาด',
          message: `เกิดข้อผิดพลาดในการอนุมัติ: ${error.message}`,
          isError: true
        }
      }));
    } finally {
      // Notify modal to close and reset button
      document.dispatchEvent(new CustomEvent('api:request-finished'));
    }
  }

  // Function to call the return/reject API
  async function rejectRequest(requestId, reason) {
    if (!reason || reason.trim() === '') {
      document.dispatchEvent(new CustomEvent('ui:show-status', {
        detail: { title: 'ข้อมูลไม่ครบถ้วน', message: 'กรุณาระบุเหตุผลการตีกลับ', isError: true }
      }));
      document.dispatchEvent(new CustomEvent('api:request-finished')); // Reset button in modal
      return;
    }

    try {
      const response = await fetch(`/api/staff/logs/${requestId}/returned`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          // Include Authorization header if needed
        },
        body: JSON.stringify({ note: reason }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      document.dispatchEvent(new CustomEvent('ui:show-status', {
        detail: { title: 'สำเร็จ', message: 'ตีกลับคำร้องสำเร็จ', isError: false }
      }));
      fetchAndCategorizeReservations(); // Refresh data
    } catch (error) {
      console.error('Reject request failed:', error);
      document.dispatchEvent(new CustomEvent('ui:show-status', {
        detail: {
          title: 'เกิดข้อผิดพลาด',
          message: `เกิดข้อผิดพลาดในการตีกลับ: ${error.message}`,
          isError: true
        }
      }));
    } finally {
      // Notify modal to close and reset button
      document.dispatchEvent(new CustomEvent('api:request-finished'));
    }
  }

  // ===== Data Fetching and Processing =====

  // แปลง YYYY-MM-DD เป็น DD/MM/YYYY (พ.ศ.)
  function formatThaiDate(isoDate) {
    if (!isoDate) return '-';
    const [year, month, day] = isoDate.split('-');
    const thaiYear = parseInt(year, 10) + 543;
    return `${day}/${month}/${thaiYear}`;
  }

  // Helper to format "HHMM" string to "HH:MM"
  function formatHHMM(timeStr) {
    if (timeStr && timeStr.length === 4) {
      return `${timeStr.substring(0, 2)}:${timeStr.substring(2, 4)}`;
    }
    return timeStr;
  }

  // แปลง slot codes เป็นช่วงเวลาที่อ่านง่าย
  function formatTimeSlots(slots) {
    if (!slots || slots.length === 0) return '-';

    const times = slots
      .map(s => s.slotCode.replace('S', '').replace(/_/g, ':'))
      .sort();

    if (times.length === 1) {
      const [start, end] = times[0].split(':');
      return `${formatHHMM(start)}-${formatHHMM(end)}`;
    }

    // หาเวลาเริ่มต้นของ slot แรก และเวลาสิ้นสุดของ slot สุดท้าย
    const startTime = times[0].split(':')[0];
    const endTime = times[times.length - 1].split(':')[1];
    return `${formatHHMM(startTime)}-${formatHHMM(endTime)}`;
  }

  // Helper to parse reason fields that might be JSON strings
  function parseReason(reasonString) {
    if (!reasonString) return '';
    try {
      // Attempt to parse the string as JSON
      const parsed = JSON.parse(reasonString);
      // If it has a 'note' property, return that. Otherwise, return the original string.
      return parsed.note || reasonString;
    } catch (e) {
      // If parsing fails, it's likely a plain string. Return it as is.
      return reasonString;
    }
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

    // แสดงเหตุผลการตีกลับ/ปฏิเสธ สำหรับแท็บ pending และ completed
    const reasonDetailsHTMLForRequest = (tabKey === 'pending' || tabKey === 'completed') ? `
      ${(request.step === 'RETURNED_FOR_FIX' && request.returnReason) ? `
        <div class="detail-row-group status-box status-box--returned">
          <div class="detail-row"><strong>เหตุผลที่ตีกลับ</strong><div>${parseReason(request.returnReason)}</div></div>
        </div>
      ` : ''}
      ${(request.finalStatus === 'REJECTED' && request.rejectReason) ? `
        <div class="detail-row-group status-box status-box--rejected">
          <div class="detail-row"><strong>เหตุผลที่ปฏิเสธ</strong><div>${parseReason(request.rejectReason)}</div></div>
        </div>
      ` : ''}
    ` : '';


    return `
      <div class="list-item" data-request-id="${request.reservationId}">
        <div class="item-grid">
          <div class="item-cell">${formatThaiDate(request.reservationDate)}</div>
          <div class="item-cell">${request.reservationId}</div>
          <div class="item-cell">${request.userName}</div>
          <div class="item-cell">${request.roomCode}</div>
          <div class="item-cell item-right">
            <span class="time">${formatTimeSlots(request.slots)}</span>
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
                <div class="detail-row"><strong>ชื่อ-นามสกุล</strong><div>${request.userName}</div></div>
                <div class="detail-row"><strong>อีเมล</strong><div>${request.userEmail}</div></div>
              </div>
              <div class="detail-row-group">
                <div class="detail-row"><strong>วันที่ต้องการใช้ห้อง</strong><div>${formatThaiDate(request.reservationDate)}</div></div>
                <div class="detail-row"><strong>ช่วงเวลา</strong><div>${formatTimeSlots(request.slots)}</div></div>
              </div>
              <div class="detail-row-group">
                <div class="detail-row"><strong>ประเภทห้อง</strong><div>ห้องเรียน</div></div>
                <div class="detail-row"><strong>ห้อง</strong><div>${request.roomCode}</div></div>
              </div>
              ${reasonDetailsHTMLForRequest}
            </div>
            <div class="details-right">
              <h3 class="details-title accent">กรุณากรอกข้อมูลเพิ่มเติม</h3>
              <div class="detail-row-group">
                <div class="detail-row"><strong>กลุ่มผู้ยื่นคำร้อง<span class="required-star">*</span></strong><div>-</div></div>
                <div class="detail-row"><strong>เบอร์โทรศัพท์<span class="required-star">*</span></strong><div>-</div></div>
              </div>
              <div class="detail-row"><strong>จุดประสงค์การขอใช้ห้อง<span class="required-star">*</span></strong><div class="purpose">${request.reason || '-'}</div></div>
              <div class="file-upload">
                <label for="doc-${request.reservationId}">อัปโหลดเอกสารเพิ่มเติม</label>
                <input id="doc-${request.reservationId}" type="text" value="${request.fileAttachment || 'ไม่มีไฟล์แนบ'}" readonly />
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

    setTimeout(() => {
      renderRequests(listContainer, allReservations[tabKey] || [], tabKey);
    }, 100); // หน่วงเวลาเล็กน้อยเพื่อให้ UI อัปเดต
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

  async function fetchAndCategorizeReservations() {
    const listContainer = document.querySelector(SELECTORS.listContainer);
    try {
      const response = await fetch('/api/staff/reservations');
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();

      // Clear previous data
      allReservations.new = [];
      allReservations.pending = [];
      allReservations.completed = [];

      // Categorize data
      data.forEach(reservation => {
        if (['APPROVED', 'REJECTED', 'CANCELLED'].includes(reservation.finalStatus)) {
          allReservations.completed.push(reservation);
        } else if (reservation.step === 'SUBMITTED' && reservation.finalStatus === 'PENDING') {
          allReservations.new.push(reservation);
        } else {
          allReservations.pending.push(reservation);
        }
      });

      // Render the currently active tab with new data
      const activeTab = document.querySelector('.tab.tab--active');
      const activeTabKey = activeTab ? activeTab.dataset.tabKey : 'new';
      renderRequests(listContainer, allReservations[activeTabKey], activeTabKey);

    } catch (error) {
      console.error('Failed to fetch reservations:', error);
      listContainer.innerHTML = `<div class="list-item error" role="alert"><div class="item-grid" style="display:block; text-align:center;">เกิดข้อผิดพลาดในการโหลดข้อมูล</div></div>`;
    }
  }

  // Public init
  function init() {
    initDelegation();
    initTabs();
    fetchAndCategorizeReservations();

    // Listen for confirmation events from modals
    document.addEventListener('modal:confirm-approve', (e) => {
      approveRequest(e.detail.requestId);
    });

    document.addEventListener('modal:confirm-reject', (e) => {
      rejectRequest(e.detail.requestId, e.detail.reason);
    });
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
