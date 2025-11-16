document.addEventListener("DOMContentLoaded", function () {
(function () {
  'use strict';

  const btn = document.getElementById('notifToggle');
  const panel = document.getElementById('notifPanel');

  if (!btn || !panel) {
    console.warn("notifToggle or notifPanel not found");
    return;
  }

  function openPanel() {
    btn.setAttribute('aria-expanded', 'true');
    panel.classList.add('open');

    const first = panel.querySelector('.notif-item');
    if (first) first.focus();

    document.documentElement.classList.add('notif-open');
  }

  function closePanel() {
    btn.setAttribute('aria-expanded', 'false');
    panel.classList.remove('open');
    document.documentElement.classList.remove('notif-open');
  }

  function togglePanel() {
    if (panel.classList.contains('open')) closePanel();
    else openPanel();
  }

  // เปิด / ปิด panel
  btn.addEventListener('click', (e) => {
    e.preventDefault();
    togglePanel();
  });

  // เมื่อกดแจ้งเตือน
  panel.addEventListener('click', (e) => {
    const item = e.target.closest('.notif-item');
    if (!item) return;

    const id = item.dataset.id || null;

    document.dispatchEvent(
      new CustomEvent('notification:clicked', { detail: { id, item } })
    );

    item.classList.remove('unread');
    updateBadge();
    closePanel();
  });

  // ปิดเมื่อคลิกนอก
  document.addEventListener('click', (e) => {
    if (panel.classList.contains('open')) {
      if (!panel.contains(e.target) && !btn.contains(e.target)) {
        closePanel();
      }
    }
  });

  // Badge unread
  function updateBadge() {
    const unread = panel.querySelectorAll('.notif-item.unread').length;
    const badge = btn.querySelector('.badge-dot');
    if (!badge) return;
    badge.style.display = unread > 0 ? 'inline-block' : 'none';
  }

  updateBadge();

  window.NotificationsUI = { openPanel, closePanel, togglePanel, updateBadge };

})();
});
