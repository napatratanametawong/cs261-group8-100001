// submit.js — intercept submit to POST and redirect to success page
(() => {
  function getAuthHeaders(){
    const token =
      (typeof localStorage!=='undefined' && (localStorage.getItem('token') || localStorage.getItem('accessToken'))) ||
      (typeof sessionStorage!=='undefined' && (sessionStorage.getItem('token') || sessionStorage.getItem('accessToken')));
    return token ? { Authorization: `Bearer ${token}` } : {};
  }
  const API_BASE = '';
  const API_PREFIX = '/api/rooms';
  const ENDPOINTS = {
    createReservation: `${API_BASE}${API_PREFIX}/reservations`
  };

  document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('.booking-card form');
    if(!form) return;

    // Handle cancel: go back to room selection page
    try{
      const cancelBtn = form.querySelector('.actions .btn-ghost');
      if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => {
          e.preventDefault();
          // Do NOT clear bookingSelection so user can reselect easily
          location.href = '../homepage_user.html';
        });
      }
    }catch{}

    // Capture-phase listener to prevent other submit handlers
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      e.stopImmediatePropagation();
      try{
        const fd = new FormData(form);
        const fileInput = form.querySelector('input[type="file"]');
        if (fileInput && fileInput.files && fileInput.files.length>0){
          Array.from(fileInput.files).forEach(f => fd.append('attachments', f, f.name));
        }
        let sel = {};
        try{ sel = JSON.parse(sessionStorage.getItem('bookingSelection')||'{}'); }catch{}
        const slots = Array.isArray(sel?.slots) ? sel.slots : [];
        fd.set('room_code', sel?.roomCode || '');
        fd.set('room_name', sel?.roomName || '');
        fd.set('date', sel?.dateISO || '');
        slots.forEach(sc => fd.append('slot_code', sc));

        const res = await fetch(ENDPOINTS.createReservation, { method:'POST', body: fd, credentials:'include', headers:{ ...getAuthHeaders() } });
        if(!res.ok) throw new Error(`HTTP ${res.status}`);
        try{ await res.json(); }catch{}
        // clear selection and go to success
        try{ sessionStorage.removeItem('bookingSelection'); }catch{}
        location.href = 'success.html';
      }catch(err){
        console.error('Submit failed', err);
        alert('ส่งคำขอไม่สำเร็จ กรุณาลองใหม่');
      }
    }, true); // capture = true
  });
})();
