// js/modals.js
(function(){
  'use strict';

  // elements
  const backdrop = document.getElementById('modalBackdrop');
  const rejectModal = document.getElementById('rejectModal');
  const approveModal = document.getElementById('approveModal');
  const statusModal = document.getElementById('statusModal');
  const rejectCancel = document.getElementById('rejectCancel');
  const rejectConfirm = document.getElementById('rejectConfirm');
  const approveCancel = document.getElementById('approveCancel');
  const approveConfirm = document.getElementById('approveConfirm');
  const rejectReason = document.getElementById('rejectReason');
  const statusTitle = document.getElementById('statusTitle');
  const statusMessage = document.getElementById('statusMessage');
  const statusClose = document.getElementById('statusClose');

  if (!backdrop || !rejectModal || !approveModal || !statusModal) return;

  // internal state: current target list-item
  let currentRequestId = null;

  // --- UI Feedback Helpers ---
  function setButtonLoading(button, isLoading) {
    if (isLoading) {
      button.disabled = true;
      button.classList.add('loading');
      // You can add a spinner inside the button if you have one
      // button.innerHTML = '<span class="spinner"></span> กำลังบันทึก...';
    } else {
      button.disabled = false;
      button.classList.remove('loading');
      // Restore original text if you changed it
    }
  }

  // open/close helpers
  function openModal(modal) {
    modal.hidden = false;
    modal.classList.add('open');
    backdrop.hidden = false;
    backdrop.classList.add('show');
    document.body.classList.add('modal-open'); // Prevent background scroll
    // focus first input or confirm button
    const focusable = modal.querySelector('textarea, button');
    if (focusable) focusable.focus();
  }

  function closeModal(modal) {
    modal.classList.remove('open');
    modal.hidden = true;

    // Check if any other modal is still open before hiding the backdrop
    const isAnyModalOpen = document.querySelector('.modal.open');
    if (!isAnyModalOpen) {
      backdrop.classList.remove('show');
      backdrop.hidden = true;
      document.body.classList.remove('modal-open'); // Restore scroll
    }

    currentRequestId = null;
  }

  // Listen for events from dashboard.js to open modals
  document.addEventListener('request:approve', (e) => {
    currentRequestId = e.detail.requestId;
    openModal(approveModal);
  });

  document.addEventListener('request:reject', (e) => {
    currentRequestId = e.detail.requestId;
    if (rejectReason) {
      rejectReason.value = '';
      rejectReason.style.boxShadow = ''; // Clear previous validation styles
    }
    openModal(rejectModal);
  });
  // backdrop click closes any open modal
  backdrop.addEventListener('click', () => {
    if (rejectModal.classList.contains('open')) closeModal(rejectModal);
    if (approveModal.classList.contains('open')) closeModal(approveModal);
    if (statusModal.classList.contains('open')) closeModal(statusModal);
  });

  // cancel buttons
  rejectCancel.addEventListener('click', () => closeModal(rejectModal));
  approveCancel.addEventListener('click', () => closeModal(approveModal));

  // confirm approve
  approveConfirm.addEventListener('click', () => {
    if (!currentRequestId || approveConfirm.disabled) { return; }
    // dispatch custom event with requestId
    const event = new CustomEvent('modal:confirm-approve', { detail: { requestId: currentRequestId } });
    document.dispatchEvent(event);
    setButtonLoading(approveConfirm, true); // Show loading state
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
    if (!currentRequestId || rejectConfirm.disabled) { return; }
    const event = new CustomEvent('modal:confirm-reject', { detail: { requestId: currentRequestId, reason } });
    document.dispatchEvent(event);
    setButtonLoading(rejectConfirm, true); // Show loading state
  });

  // close on Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      if (rejectModal.classList.contains('open')) closeModal(rejectModal);
      if (approveModal.classList.contains('open')) closeModal(approveModal);
      if (statusModal.classList.contains('open')) closeModal(statusModal);
    }
  });

  // Listen for completion events from dashboard.js to close modal and reset buttons
  document.addEventListener('api:request-finished', () => {
    if (approveModal.classList.contains('open')) {
      setButtonLoading(approveConfirm, false);
      closeModal(approveModal);
    }
    if (rejectModal.classList.contains('open')) {
      setButtonLoading(rejectConfirm, false);
      closeModal(rejectModal);
    }
  });

  // --- Status Modal Logic ---
  function showStatusModal(title, message, isError = false) {
    if (!statusModal || !statusTitle || !statusMessage) return;

    statusTitle.textContent = title;
    statusMessage.textContent = message;

    const card = statusModal.querySelector('.modal-card');
    if (isError) {
      card.classList.add('error');
    } else {
      card.classList.remove('error');
    }
    openModal(statusModal);
  }

  statusClose.addEventListener('click', () => closeModal(statusModal));

  document.addEventListener('ui:show-status', (e) => {
    const { title, message, isError } = e.detail;
    showStatusModal(title, message, isError);
  });

  // optional: expose helpers
  window.ModalRequests = {
    // These are now handled by events, but could be kept for debugging
    closeAll: () => { closeModal(rejectModal); closeModal(approveModal); }
  };

})();
