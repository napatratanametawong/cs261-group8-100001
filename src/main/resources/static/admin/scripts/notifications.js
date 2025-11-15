// js/notifications.js
// Toggle notification dropdown from bell icon, accessible, close on outside click.
// Dispatches custom event 'notification:clicked' when an item is selected.

(function() {
  'use strict';
  const btn = document.getElementById('notifToggle');
  const panel = document.getElementById('notifPanel');

  if (!btn || !panel) return;

  function openPanel() {
    btn.setAttribute('aria-expanded', 'true');
    panel.classList.add('open');
    // focus first item
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

  btn.addEventListener('click', (e) => {
    e.preventDefault();
    togglePanel();
  });

  btn.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      togglePanel();
    }
    if (e.key === 'Escape') closePanel();
  });

  // delegate click inside panel
  panel.addEventListener('click', (e) => {
    const item = e.target.closest('.notif-item');
    if (!item) return;
    const id = item.dataset.id || null;
    // dispatch event for app code to handle
    document.dispatchEvent(new CustomEvent('notification:clicked', { detail: { id, item } }));
    // mark read
    item.classList.remove('unread');
    // optionally remove badge-dot if none unread
    updateBadge();
    // close panel
    closePanel();
  });

  // close on outside click
  document.addEventListener('click', (e) => {
    if (panel.classList.contains('open')) {
      if (!panel.contains(e.target) && !btn.contains(e.target)) {
        closePanel();
      }
    }
  });

  // close on Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && panel.classList.contains('open')) closePanel();
  });

  // close on resize
  window.addEventListener('resize', () => {
    if (panel.classList.contains('open')) closePanel();
  });

  function updateBadge() {
    const unread = panel.querySelectorAll('.notif-item.unread').length;
    const badge = btn.querySelector('.badge-dot');
    if (!badge) return;
    if (unread > 0) badge.style.display = 'inline-block';
    else badge.style.display = 'none';
  }

  // initial badge state
  updateBadge();

  // expose functions
  window.NotificationsUI = { openPanel, closePanel, togglePanel, updateBadge };
})();
