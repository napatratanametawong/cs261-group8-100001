document.addEventListener("DOMContentLoaded", function () {
(function () {
  'use strict';

  console.log("🚀 Notification API Loaded");

  const notifPanel = document.getElementById('notifPanel');
  const notifList  = notifPanel?.querySelector('.notif-list');

  if (!notifPanel || !notifList) {
    console.warn("notifPanel/notifList not found");
    return;
  }

  // ดึง email staff จาก localStorage
  let staffEmail = localStorage.getItem("userEmail");
  if (!staffEmail) {
    console.warn("⚠ ไม่มี userEmail ใน localStorage — ใช้ default เพื่อ debug");
    staffEmail = "lc2.serviceadm@gmail.com"; 
  }

  // -------------------------------------------------
  // 1) LOAD NOTIFICATIONS (GET)
  // -------------------------------------------------
  async function loadNotifications() {
    console.log("🔍 Fetching notifications...");

    try {
      const res = await fetch(`/api/staff/notifications?email=${staffEmail}`);
      
      if (!res.ok) throw new Error("Load failed");

      const data = await res.json();
      notifList.innerHTML = "";

      if (data.length === 0) {
        notifList.innerHTML = `<div class="notif-item empty">ไม่มีการแจ้งเตือน</div>`;
        window.NotificationsUI.updateBadge();
        return;
      }

      // เติมรายการแจ้งเตือน
      data.forEach(n => {
        const el = document.createElement("button");
        el.className = `notif-item ${n.read ? "" : "unread"}`;
        el.type = "button";
        el.dataset.id = n.id;

        el.innerHTML = `
          <div class="title">${n.title}</div>
          <div class="meta">${timeAgo(n.createdAt)}</div>
        `;

        notifList.appendChild(el);
      });

      window.NotificationsUI.updateBadge();
      console.log("✅ Notifications loaded:", data.length);

    } catch (err) {
      console.error("❌ Error loading notifications:", err);
      notifList.innerHTML = `<div class="notif-item empty">โหลดข้อมูลล้มเหลว</div>`;
    }
  }

  // -------------------------------------------------
  // 2) MARK AS READ (PUT)
  // -------------------------------------------------
  async function markAsRead(id) {
    try {
      console.log(`📨 Mark as read ID = ${id}`);

      await fetch(`/api/staff/notifications/${id}/read?email=${staffEmail}`, {
        method: "PUT"
      });

    } catch (err) {
      console.error("❌ Mark read failed:", err);
    }
  }

  // -------------------------------------------------
  // 3) รับ event จาก notifications.js
  // -------------------------------------------------
  document.addEventListener("notification:clicked", (e) => {
    const id = e.detail.id;
    if (!id) return;

    markAsRead(id);
  });

  // -------------------------------------------------
  // 4) Format time “xx นาทีที่แล้ว”
  // -------------------------------------------------
  function timeAgo(iso) {
    const now = new Date();
    const d = new Date(iso);
    const diff = (now - d) / 1000;

    const minutes = Math.floor(diff / 60);
    const hours = Math.floor(diff / 3600);
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days} วันที่แล้ว`;
    if (hours > 0) return `${hours} ชั่วโมงที่แล้ว`;
    if (minutes > 0) return `${minutes} นาทีที่แล้ว`;
    return "เมื่อสักครู่";
  }

  // -------------------------------------------------
  // 5) โหลดตอนเปิดเว็บ
  // -------------------------------------------------
  loadNotifications();

})();
});
