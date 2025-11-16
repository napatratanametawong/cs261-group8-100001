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
// js/notifications-api.js
// Load staff notifications + create notif-item button + mark as read

(function () {
  'use strict';

  const notifPanel = document.getElementById('notifPanel');
  const notifList = notifPanel?.querySelector('.notif-list');
  const staffEmail = localStorage.getItem("userEmail"); // ตั้งค่าไว้ตอน login

  if (!notifPanel || !notifList || !staffEmail) return;

  // -----------------------------
  // 1) Load notifications (GET)
  // -----------------------------
  async function loadNotifications() {
    try {
      const res = await fetch(`/api/staff/notifications?email=${staffEmail}`);
      if (!res.ok) throw new Error("Load notifications failed");

      const data = await res.json();
      notifList.innerHTML = ""; // ล้างของเก่า

      if (!data.length) {
        notifList.innerHTML = `<div class="notif-item empty">ไม่มีการแจ้งเตือน</div>`;
        window.NotificationsUI.updateBadge();
        return;
      }

      data.forEach(n => {
        const el = document.createElement("button");
        el.className = `notif-item ${n.read ? "" : "unread"}`;
        el.dataset.id = n.id;
        el.type = "button";

        el.innerHTML = `
          <div class="title">${n.title}</div>
          <div class="meta">${timeAgo(n.createdAt)}</div>
        `;

        notifList.appendChild(el);
      });

      window.NotificationsUI.updateBadge();
    } catch (err) {
      console.error(err);
      notifList.innerHTML = `<div class="notif-item empty">โหลดข้อมูลล้มเหลว</div>`;
    }
  }

  // -----------------------------
  // 2) Mark notification as read
  // -----------------------------
  async function markAsRead(id) {
    try {
      await fetch(`/api/staff/notifications/${id}/read?email=${staffEmail}`, {
        method: "PUT"
      });
    } catch (err) {
      console.error("Mark read failed", err);
    }
  }

  // -----------------------------
  // 3) Listen click from notifications.js
  // -----------------------------
  document.addEventListener("notification:clicked", (e) => {
    const id = e.detail.id;
    if (!id) return;
    markAsRead(id);
  });

  // -----------------------------
  // 4) Utility - format time
  // -----------------------------
  function timeAgo(isoString) {
    const now = new Date();
    const date = new Date(isoString);
    const diffMs = now - date;

    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor(diffMs / (1000 * 60));

    if (diffHours >= 24) {
      const diffDays = Math.floor(diffHours / 24);
      return `${diffDays} วันที่แล้ว`;
    }
    if (diffHours >= 1) return `${diffHours} ชั่วโมงที่แล้ว`;
    if (diffMinutes >= 1) return `${diffMinutes} นาทีที่แล้ว`;
    return "เมื่อสักครู่";
  }

  // โหลดตอนเปิดเว็บ
  loadNotifications();

})();
