// submit.js — intercept submit to POST and redirect to success page
(() => {
  function getAuthHeaders() {
    const token =
      (typeof localStorage !== 'undefined' && (localStorage.getItem('token') || localStorage.getItem('accessToken'))) ||
      (typeof sessionStorage !== 'undefined' && (sessionStorage.getItem('token') || sessionStorage.getItem('accessToken')));
    return token ? { Authorization: `Bearer ${token}` } : {};
  }
  const API_BASE = '';
  const API_PREFIX = '/api';
  const ENDPOINTS = {
    createReservation: `${API_BASE}${API_PREFIX}/reservations`
  };
  // TEMP: force redirect to error page after successful submit for demo/testing.
  // To revert to original behavior, set to false or remove this flag.
  const TEMP_FORCE_ERROR_REDIRECT = false;

  document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('.booking-card form');
    if (!form) return;

    // ---------- [NEW] pick required fields & submit button ----------
    const submitBtn = form.querySelector('.actions .btn.btn-primary');
    const phoneEl = form.querySelector('input[name="phone"]');     // บังคับ 10 หลัก
    const reasonEl = form.querySelector('textarea[name="reason"]'); // บังคับไม่ว่าง
    const orgEl = form.querySelector('input[name="org"]');       // ช่องแรก: ไม่บังคับ

    // ช่องแรกไม่บังคับแน่นอน
    if (orgEl) orgEl.removeAttribute('required');

    // sanitize เบอร์ = เก็บไว้แค่ตัวเลข 10 หลัก
    const sanitizePhone = () => {
      if (!phoneEl) return;
      const clean = (phoneEl.value || '').replace(/\D/g, '').slice(0, 10);
      if (phoneEl.value !== clean) phoneEl.value = clean;
    };

    // เปิด/ปิดปุ่มตามความครบของ "เฉพาะช่องที่บังคับ"
    const recalc = () => {
      sanitizePhone();
      const phoneOK = !!phoneEl && phoneEl.value.length === 10;
      const reasonOK = !!reasonEl && reasonEl.value.trim().length > 0;
      const ok = phoneOK && reasonOK;
      if (submitBtn) {
        submitBtn.disabled = !ok;            // << ปลดเทาทันทีเมื่อครบ
        submitBtn.classList.toggle('disabled', !ok); // เผื่อมีสไตล์ .disabled
      }
    };

    // ติดตามการเปลี่ยนค่า
    form.addEventListener('input', recalc);
    form.addEventListener('change', recalc);
    // run ครั้งแรก
    recalc();

    // Handle cancel: go back to room selection page
    try {
      const cancelBtn = form.querySelector('.actions .btn-ghost');
      if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => {
          e.preventDefault();
          // Do NOT clear bookingSelection so user can reselect easily
          location.href = '/bookingRoom/homepage_user.html';
        });
      }
    } catch { }

    // Capture-phase listener to prevent other submit handlers
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      e.stopImmediatePropagation();

      // ---------- [NEW] client-side guard: ตรวจอีกชั้นก่อนส่ง ----------
      sanitizePhone();
      if (phoneEl) {
        if (phoneEl.value.length !== 10) {
          phoneEl.setCustomValidity('กรุณากรอกเบอร์โทรศัพท์เป็นตัวเลข 10 หลัก');
          phoneEl.reportValidity();
          phoneEl.focus();
          return;
        } else {
          phoneEl.setCustomValidity('');
        }
      }
      if (reasonEl) {
        if (!reasonEl.value.trim()) {
          reasonEl.setCustomValidity('กรุณากรอกวัตถุประสงค์ในการใช้งาน');
          reasonEl.reportValidity();
          reasonEl.focus();
          return;
        } else {
          reasonEl.setCustomValidity('');
        }
      }

      try {
        // NEW: Submit JSON to /api/reservations (backend DTO)
        const textarea = form.querySelector('textarea');
        const fileInput = form.querySelector('input[type="file"]');
        const readFirstFileAsDataURL = (inp) => new Promise((resolve) => {
          try {
            if (!inp || !inp.files || inp.files.length === 0) return resolve(null);
            const file = inp.files[0];
            const fr = new FileReader();
            fr.onload = () => resolve(String(fr.result || ''));
            fr.onerror = () => resolve(null);
            fr.readAsDataURL(file);
          } catch { resolve(null); }
        });
        let profile = {};
        try { profile = JSON.parse(localStorage.getItem('profile') || '{}'); } catch { }
        let sel = {};
        try { sel = JSON.parse(sessionStorage.getItem('bookingSelection') || '{}'); } catch { }
        const slotCodes = Array.isArray(sel?.slots) ? sel.slots : [];
        const payload = {
          roomCode: sel?.roomCode || '',
          reservationDate: sel?.dateISO || '',
          slotCodes,
        };
        const reason = (textarea?.value || '').trim();
        if (reason) payload.reason = reason;
        const userEmail = (profile?.email || '').trim();
        if (userEmail) payload.userEmail = userEmail;
        const userName = (profile?.displayname_th || profile?.userName || '').trim();
        if (userName) payload.userName = userName;
        const fileDataUrl = await readFirstFileAsDataURL(fileInput);
        if (fileDataUrl) payload.fileAttachment = fileDataUrl;

        // (ถ้าต้องการส่งเบอร์โทรด้วย ให้ uncomment บรรทัดถัดไป)
        // if (phoneEl && phoneEl.value) payload.phone = phoneEl.value;

        const res = await fetch(ENDPOINTS.createReservation, {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
          body: JSON.stringify(payload),
        });
        if (!res.ok) {
          let msg = `HTTP ${res.status}`;
          try { const err = await res.json(); if (err?.message) msg = err.message; } catch { }
          throw new Error(msg);
        }
        const data = await res.json().catch(() => null);
        try { sessionStorage.removeItem('bookingSelection'); } catch { }
        try { if (data) sessionStorage.setItem('lastReservation', JSON.stringify(data)); } catch { }
        if (TEMP_FORCE_ERROR_REDIRECT) {
          try { sessionStorage.setItem('lastReservationError', 'การยื่นคำร้องไม่สำเร็จ (โหมดทดสอบ)'); } catch { }
          location.href = 'error.html';
          return;
        }
        location.href = 'success.html';
      } catch (err) {
        console.error('Submit failed', err);
        try { sessionStorage.setItem('lastReservationError', (err && err.message) ? String(err.message) : 'เกิดข้อผิดพลาดภายในระบบ กรุณาลองใหม่อีกครั้ง'); } catch { }
        location.href = 'error.html';
        return;
        // NOTE: โค้ดด้านล่างนี้จะไม่ทำงานเพราะ return ไปแล้ว
        // alert('ส่งคำขอไม่สำเร็จ กรุณาลองใหม่');
      }
    }, true); // capture = true
  });
})();
