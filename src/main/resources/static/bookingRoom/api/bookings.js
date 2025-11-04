// bookings.js — enable Book only when at least one slot is selected
(function(){
  function cardFromRoomCode(code){
    return document.querySelector(`.room-card .btn-book[data-room-code="${CSS.escape(code)}"]`)?.closest('.room-card') || null;
  }

  function updateButtonStateForCard(card){
    if(!card) return;
    const btn = card.querySelector('.btn-book');
    if(!btn) return;
    const anyPick = card.querySelector('.slots .slot-label.is-selected') !== null;
    btn.disabled = !anyPick;
    btn.setAttribute('aria-disabled', btn.disabled ? 'true' : 'false');
  }

  // When slots are rendered/enhanced, ensure initial disabled state
  function initAll(){
    document.querySelectorAll('.room-card').forEach(updateButtonStateForCard);
  }

  // React to any change in a room's selection
  window.addEventListener('timeslot:change', (ev)=>{
    const code = ev?.detail?.roomCode || '';
    const card = code ? cardFromRoomCode(code) : null;
    if(card){ updateButtonStateForCard(card); }
  });

  // Also run after rooms are (re)rendered
  window.addEventListener('rooms:rendered', initAll);
  if (document.readyState === 'complete' || document.readyState === 'interactive') {
    initAll();
  } else {
    document.addEventListener('DOMContentLoaded', initAll);
  }

  function getSelectedLabels(card){
    const selected = Array.from(card.querySelectorAll('.slots .slot-label.is-selected'));
    const texts = selected.map(el => (el.querySelector('.slot-text')?.textContent || el.textContent || '').trim()).filter(Boolean);
    return texts;
  }

  // Navigate to the form when clicking Book (only if enabled)
  document.addEventListener('click', (e)=>{
    const btn = e.target.closest('.btn-book');
    if(!btn) return;
    if(btn.disabled) return; // guard
    const card = btn.closest('.room-card');
    const picks = Array.from(card.querySelectorAll('.slots .slot-label.is-selected'))
      .map(el=>el.dataset.slotCode).filter(Boolean);
    if(!picks.length) return;

    const dateISO = (window.CalendarAPI && typeof window.CalendarAPI.getSelectedISO==='function')
      ? window.CalendarAPI.getSelectedISO() : '';
    const dateText = (function(){
      if(!dateISO) return '';
      try{ const d=new Date(dateISO); return d.toLocaleDateString('th-TH'); }catch{ return dateISO; }
    })();
    const labels = getSelectedLabels(card);
    const sel = {
      roomCode: btn.dataset.roomCode || '',
      roomName: btn.dataset.roomName || '',
      roomType: (card?.dataset?.category || ''),
      slots: picks,
      dateISO,
      dateText,
      timeText: labels.join(', '),
      timeList: labels
    };
    try{ sessionStorage.setItem('bookingSelection', JSON.stringify(sel)); }catch{}

    // go to the form page relative to homepage_user.html
    location.href = 'form/index.html';
  });
})();
