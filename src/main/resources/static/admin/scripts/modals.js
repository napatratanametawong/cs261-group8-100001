// js/modals.js
(function(){
  'use strict';

  // elements
  const backdrop = document.getElementById('modalBackdrop');
  const rejectModal = document.getElementById('rejectModal');
  const approveModal = document.getElementById('approveModal');
  const rejectCancel = document.getElementById('rejectCancel');
  const rejectConfirm = document.getElementById('rejectConfirm');
  const approveCancel = document.getElementById('approveCancel');
  const approveConfirm = document.getElementById('approveConfirm');
  const rejectReason = document.getElementById('rejectReason');

  if (!backdrop || !rejectModal || !approveModal) return;

  // internal state: current target list-item
  let currentTargetLi = null;

  // open/close helpers
  function openModal(modal) {
    modal.hidden = false;
    modal.classList.add('open');
    backdrop.hidden = false;
    backdrop.classList.add('show');
    document.documentElement.classList.add('modal-open');
    // focus first input or confirm button
    const focusable = modal.querySelector('textarea, button');
    if (focusable) focusable.focus();
  }

  function closeModal(modal) {
    modal.classList.remove('open');
    modal.hidden = true;
    backdrop.classList.remove('show');
    backdrop.hidden = true;
    document.documentElement.classList.remove('modal-open');
    // clear state
    currentTargetLi = null;
  }

  // attach to actions in list: delegate clicks on accept/reject
  document.addEventListener('click', function(ev){
    const accept = ev.target.closest('.btn-accept');
    if (accept) {
      const li = accept.closest('.list-item');
      currentTargetLi = li;
      openModal(approveModal);
      return;
    }
    const reject = ev.target.closest('.btn-reject');
    if (reject) {
      const li = reject.closest('.list-item');
      currentTargetLi = li;
      // reset textarea
      if (rejectReason) rejectReason.value = '';
      openModal(rejectModal);
      return;
    }
  });

  // backdrop click closes any open modal
  backdrop.addEventListener('click', () => {
    if (rejectModal.classList.contains('open')) closeModal(rejectModal);
    if (approveModal.classList.contains('open')) closeModal(approveModal);
  });

  // cancel buttons
  rejectCancel.addEventListener('click', () => closeModal(rejectModal));
  approveCancel.addEventListener('click', () => closeModal(approveModal));

  // confirm approve
  approveConfirm.addEventListener('click', () => {
    if (!currentTargetLi) { closeModal(approveModal); return; }
    const requestId = currentTargetLi.dataset.requestId || null;
    // dispatch custom event with requestId
    document.dispatchEvent(new CustomEvent('request:accepted:confirm', { detail: { requestId, source: currentTargetLi } }));
    // close modal
    closeModal(approveModal);
  });

  // confirm reject (with validation)
  rejectConfirm.addEventListener('click', () => {
    const reason = rejectReason ? rejectReason.value.trim() : '';
    if (!reason) {
      // simple client-side feedback (focus + outline); you can replace with nicer UI
      rejectReason.focus();
      rejectReason.style.boxShadow = '0 0 0 3px rgba(204,0,31,0.12)';
      setTimeout(() => rejectReason.style.boxShadow = '', 1200);
      return;
    }
    if (!currentTargetLi) { closeModal(rejectModal); return; }
    const requestId = currentTargetLi.dataset.requestId || null;
    document.dispatchEvent(new CustomEvent('request:rejected:confirm', { detail: { requestId, reason, source: currentTargetLi } }));
    closeModal(rejectModal);
  });

  // close on Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      if (rejectModal.classList.contains('open')) closeModal(rejectModal);
      if (approveModal.classList.contains('open')) closeModal(approveModal);
    }
  });

  // prevent background scroll while modal open
  const observer = new MutationObserver(() => {
    const open = rejectModal.classList.contains('open') || approveModal.classList.contains('open');
    if (open) document.body.style.overflow = 'hidden';
    else document.body.style.overflow = '';
  });
  observer.observe(document.body, { attributes: true, subtree: true });

  // optional: expose helpers
  window.ModalRequests = {
    openRejectFor: (li) => { currentTargetLi = li; openModal(rejectModal); },
    openApproveFor: (li) => { currentTargetLi = li; openModal(approveModal); },
    closeAll: () => { closeModal(rejectModal); closeModal(approveModal); }
  };

})();
