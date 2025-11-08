(function () {
  const SELECT_ONE_PER_ROOM = false;

  const SLOT_TIME = {
    S0800_0930: { start: '08:00', end: '09:30' },
    S0930_1100: { start: '09:30', end: '11:00' },
    S1100_1230: { start: '11:00', end: '12:30' },
    S1330_1500: { start: '13:30', end: '15:00' },
    S1500_1630: { start: '15:00', end: '16:30' },
    S1630_1800: { start: '16:30', end: '18:00' },
  };
  const SLOT_ORDER = Object.keys(SLOT_TIME);

  function getRoomMeta(roomCardEl) {
    const btn = roomCardEl.querySelector('.btn-book');
    return {
      roomCode: btn?.dataset.roomCode || '',
      roomName: btn?.dataset.roomName || ''
    };
  }

  // TOP bar (selected range) — stays ABOVE slots
  function ensureTopActions(slotsEl) {
    const parent = slotsEl.parentElement; // usually .info
    let actions = parent.querySelector('.slot-actions');
    if (!actions) {
      actions = document.createElement('div');
      actions.className = 'slot-actions';
      parent.insertBefore(actions, slotsEl);

      const range = document.createElement('span');
      range.className = 'selected-range';
      actions.appendChild(range);
    }
    const rangeEl = actions.querySelector('.selected-range');
    return { actions, rangeEl };
  }

  // Clear All button — appended INSIDE .slots after the last slot label
  function ensureClearButton(slotsEl) {
    let clearBtn = slotsEl.querySelector('.clear-all-btn');
    if (!clearBtn) {
      clearBtn = document.createElement('button');
      clearBtn.type = 'button';
      clearBtn.className = 'clear-all-btn';
      clearBtn.textContent = 'Clear All';
      clearBtn.addEventListener('click', () => {
        slotsEl.querySelectorAll('.slot-label.is-selected').forEach((el) => {
          el.classList.remove('is-selected');
          el.setAttribute('aria-pressed', 'false');
        });
        updateRangeAndClear(slotsEl);
      });
      slotsEl.appendChild(clearBtn); // << append after last slot
    }
    return clearBtn;
  }

  function updateRangeAndClear(slotsEl) {
    const { rangeEl } = ensureTopActions(slotsEl);
    const clearBtn = ensureClearButton(slotsEl);

    const selectedEls = Array.from(
      slotsEl.querySelectorAll('.slot-label.is-selected')
    );
    const picks = selectedEls.map(el => el.dataset.slotCode).filter(Boolean);

    if (picks.length === 0) {
      rangeEl.textContent = '';
      rangeEl.style.display = 'none';
      clearBtn.style.display = 'none';
      return;
    }

    // Sort by known order, then join individual labels so gaps are preserved
    const sorted = picks.slice().sort(
      (a, b) => SLOT_ORDER.indexOf(a) - SLOT_ORDER.indexOf(b)
    );
    const labels = sorted.map(code => {
      const el = selectedEls.find(x => x.dataset.slotCode === code);
      return (el?.querySelector('.slot-text')?.textContent || el?.textContent || '').trim();
    }).filter(Boolean);

    rangeEl.textContent = labels.length ? `เวลาที่เลือก: ${labels.join(', ')}` : '';
    rangeEl.style.display = labels.length ? '' : 'none';

    clearBtn.style.display = picks.length > 1 ? '' : 'none';

    const roomCard = slotsEl.closest('.room-card');
    const { roomCode, roomName } = getRoomMeta(roomCard || document);
    window.dispatchEvent(new CustomEvent('timeslot:range', {
      detail: { roomCode, roomName, labels, count: picks.length }
    }));
  }

  function enhanceSlots(slotsEl) {
    if (!slotsEl || slotsEl.dataset.enhanced === '1') return;
    slotsEl.dataset.enhanced = '1';

    Array.from(slotsEl.querySelectorAll('.slot')).forEach((span) => {
      const isBusy   = span.classList.contains('busy');
      const slotCode = span.dataset.slotCode || '';
      const labelTxt = (span.textContent || '').trim();
      if (!labelTxt) return;

      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = `slot slot-label ${isBusy ? 'slot-busy' : 'slot-free'}`;
      btn.dataset.slotCode = slotCode;
      btn.setAttribute('aria-disabled', isBusy ? 'true' : 'false');
      btn.setAttribute('aria-pressed', 'false');

      const check = document.createElement('span');
      check.className = 'slot-check';
      check.setAttribute('aria-hidden', 'true');

      const text = document.createElement('span');
      text.className = 'slot-text';
      text.textContent = labelTxt;

      btn.appendChild(check);
      btn.appendChild(text);

      if (!isBusy) {
        btn.addEventListener('click', () => {
          const already = btn.classList.contains('is-selected');

          if (SELECT_ONE_PER_ROOM && !already) {
            slotsEl.querySelectorAll('.slot-label.is-selected').forEach((sib) => {
              sib.classList.remove('is-selected');
              sib.setAttribute('aria-pressed', 'false');
            });
          }

          btn.classList.toggle('is-selected');
          btn.setAttribute('aria-pressed', btn.classList.contains('is-selected') ? 'true' : 'false');

          updateRangeAndClear(slotsEl);

          const roomCard = btn.closest('.room-card');
          const { roomCode, roomName } = getRoomMeta(roomCard || document);
          window.dispatchEvent(new CustomEvent('timeslot:change', {
            detail: {
              roomCode, roomName,
              slotCode,
              selected: btn.classList.contains('is-selected')
            }
          }));
        });
      } else {
        btn.addEventListener('click', (e) => e.preventDefault());
      }

      span.replaceWith(btn);
    });

    // make sure top range and clear button exist and reflect current state
    ensureTopActions(slotsEl);
    ensureClearButton(slotsEl);
    updateRangeAndClear(slotsEl);
  }

  function enhanceAll() {
    document.querySelectorAll('.room-card .slots').forEach(enhanceSlots);
  }

  function getSelections() {
    const picks = [];
    document.querySelectorAll('.room-card').forEach((card) => {
      const btn = card.querySelector('.btn-book');
      const roomCode = btn?.dataset.roomCode || '';
      const roomName = btn?.dataset.roomName || '';
      if (!roomCode) return;
      card.querySelectorAll('.slots .slot-label.is-selected').forEach((el) => {
        const slotCode = el.dataset.slotCode || '';
        if (slotCode) picks.push({ roomCode, roomName, slotCode });
      });
    });
    return picks;
  }

  window.addEventListener('rooms:rendered', enhanceAll);
  if (document.readyState === 'complete' || document.readyState === 'interactive') {
    enhanceAll();
  } else {
    document.addEventListener('DOMContentLoaded', enhanceAll);
  }

  window.TimeSelectAPI = { getSelections, reEnhance: enhanceAll };
})();
