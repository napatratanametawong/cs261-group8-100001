document.addEventListener("DOMContentLoaded", function () {
  (function () {
    'use strict';

    console.log("🚀 notifications-api.js loaded");

    const notifPanel = document.getElementById('notifPanel');
    const notifList = notifPanel?.querySelector('.notif-list');

    if (!notifPanel || !notifList) {
      console.warn("notifPanel / notifList not found");
      return;
    }

    // Email ของ staff (มาจาก login)
    let staffEmail = localStorage.getItem("userEmail");
    if (!staffEmail) {
      console.warn("⚠ ไม่มี userEmail — ใช้ default เพื่อทดสอบ");
      staffEmail = "lc2.serviceadm@gmail.com";
    }

    // ----------------------------------------------------
    // 🟩 แปลงข้อความ "Type" → ไทย พร้อมดึง #ID
    // ----------------------------------------------------
    function mapNotificationText(n) {
      const id = n.message?.match(/#(\d+)/)?.[1] || n.id;

      let titleTH = "";
      let messageTH = "";

      switch (n.notificationType) {

        case "NEW_REQUEST":
          titleTH = "มีคำขอจองใหม่เข้ามา";
          messageTH = `คำร้องหมายเลข #${id}`;
          break;

        case "USER_CANCELLED":
          titleTH = "ผู้ใช้ยกเลิกคำร้อง";
          messageTH = `คำร้องหมายเลข #${id} ถูกยกเลิก`;
          break;

        case "UPDATED_REQUEST":
          titleTH = "ผู้ใช้ส่งคำร้องฉบับแก้ไขมาใหม่";
          messageTH = `คำร้องหมายเลข #${id} ถูกแก้ไขและส่งกลับมาใหม่`;
          break;

        default:
          titleTH = n.title;
          messageTH = n.message;
      }

      return { titleTH, messageTH };
    }

    // ----------------------------------------------------
    // 🟧 โหลดแจ้งเตือนจาก API
    // ----------------------------------------------------
    async function loadNotifications() {

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

        data.forEach(n => {
          const el = document.createElement("button");
          el.className = `notif-item ${n.read ? "" : "unread"}`;
          el.type = "button";
          el.dataset.id = n.id;

          const t = mapNotificationText(n);

          // ถ้า unread ให้มี span.dot นำหน้า
          const dotHTML = n.read ? '' : '<span class="dot"></span>';

          el.innerHTML = `
    <div class="title">
      ${dotHTML}${t.titleTH}
    </div>
    <div class="meta">${t.messageTH} · ${timeAgo(n.createdAt)}</div>
  `;

          notifList.appendChild(el);
        });

        window.NotificationsUI.updateBadge();

      } catch (err) {
        console.error("❌ Load failed:", err);
        notifList.innerHTML = `<div class="notif-item empty">โหลดข้อมูลล้มเหลว</div>`;
      }
    }

    // ----------------------------------------------------
    // 🟥 API: mark-read
    // ----------------------------------------------------
    async function markAsRead(id) {
      try {
        await fetch(`/api/staff/notifications/${id}/read?email=${staffEmail}`, {
          method: "PUT"
        });

      } catch (err) {
        console.error("❌ Mark-read error:", err);
      }
    }

    // event จาก notifications.js
    document.addEventListener("notification:clicked", (e) => {
      const id = e.detail.id;
      if (!id) return;
      markAsRead(id);
    });

    // ----------------------------------------------------
    // เวลาแบบ “xx นาทีที่แล้ว”
    // ----------------------------------------------------
    function timeAgo(iso) {
      const now = new Date();
      const d = new Date(iso);
      const sec = (now - d) / 1000;

      const m = Math.floor(sec / 60);
      const h = Math.floor(sec / 3600);
      const day = Math.floor(sec / 86400);

      if (day > 0) return `${day} วันที่แล้ว`;
      if (h > 0) return `${h} ชั่วโมงที่แล้ว`;
      if (m > 0) return `${m} นาทีที่แล้ว`;
      return "เมื่อสักครู่";
    }

    // เรียกโหลดตอนเปิดหน้า
    loadNotifications();

  })();
});
