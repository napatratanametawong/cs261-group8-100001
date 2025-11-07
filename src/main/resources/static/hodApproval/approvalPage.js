
// ถ้ามีไฟล์แนบ ให้เซ็ต href ที่นี่
// document.querySelector(".file-pill").href = "https://your-storage/xxx.pdf";

// ===== Elements
const backdrop = document.getElementById('backdrop');
const approveModal = document.getElementById('approveModal');
const rejectModal  = document.getElementById('rejectModal');
const approveBtn   = document.getElementById('approveBtn');
const rejectBtn    = document.getElementById('rejectBtn');
const approveConfirmBtn = document.getElementById('approveConfirmBtn');
const rejectConfirmBtn  = document.getElementById('rejectConfirmBtn');
const rejectReason = document.getElementById('rejectReason');

let lastFocusedEl = null;

// ===== Helpers
function openModal(modalEl) {
  lastFocusedEl = document.activeElement;
  backdrop.hidden = false;
  modalEl.hidden = false;
  // โฟกัสปุ่มยกเลิกตัวแรกในโมดัล
  const firstClose = modalEl.querySelector('[data-close]') || modalEl.querySelector('button, [href], textarea');
  if (firstClose) firstClose.focus();
  document.addEventListener('keydown', onEscToClose);
  backdrop.addEventListener('click', closeAll);
  modalEl.querySelectorAll('[data-close]').forEach(b => b.addEventListener('click', closeAll));
}

function closeAll() {
  backdrop.hidden = true;
  approveModal.hidden = true;
  rejectModal.hidden  = true;
  document.removeEventListener('keydown', onEscToClose);
  backdrop.removeEventListener('click', closeAll);
  // คืนโฟกัส
  if (lastFocusedEl) lastFocusedEl.focus();
}

function onEscToClose(e) {
  if (e.key === 'Escape') closeAll();
}

// ===== Wire buttons
approveBtn.addEventListener('click', () => openModal(approveModal));
rejectBtn.addEventListener('click', () => {
  rejectReason.classList.remove('is-invalid');
  rejectReason.value = '';
  openModal(rejectModal);
});

// ===== Confirm actions (รอเชื่อม API จริง)
approveConfirmBtn.addEventListener('click', async () => {
  // TODO: fetch('/api/booking/{id}/approve', { method:'POST' })
  closeAll();
  alert('อนุมัติคำร้องเรียบร้อย (ตัวอย่าง)');
});

rejectConfirmBtn.addEventListener('click', async () => {
  const reason = rejectReason.value.trim();
  if (!reason) {
    rejectReason.classList.add('is-invalid');
    rejectReason.focus();
    return;
  }
  // TODO: fetch('/api/booking/{id}/reject', { method:'POST', body: JSON.stringify({ reason }) })
  closeAll();
  alert('ตีกลับคำร้องแล้ว (ตัวอย่าง)\\nเหตุผล: ' + reason);
});

