// booking.js — same simple placeholder
(function () {
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('.btn-book');
    if (!btn) return;
    const code = btn.dataset.roomCode || '';
    const name = btn.dataset.roomName || '';
    alert(`เริ่มกระบวนการจอง — (ยังไม่ได้เชื่อมต่อ backend)\nRoom: ${code} ${name}`);
  });
})();
