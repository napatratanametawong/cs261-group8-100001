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

    // ===== เก็บสถานะ unread เดิมไว้ใช้เทียบ =====
    let lastUnreadIds = new Set();

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
    //   isAuto = true ถ้าเป็นการ refresh แบบอัตโนมัติ
    // ----------------------------------------------------
    async function loadNotifications(isAuto = false) {
      try {
        const res = await fetch(`/api/staff/notifications?email=${staffEmail}`);
        if (!res.ok) throw new Error("Load failed");

        const data = await res.json();
        notifList.innerHTML = "";

        if (!Array.isArray(data) || data.length === 0) {
          notifList.innerHTML = `<div class="notif-item empty">ไม่มีการแจ้งเตือน</div>`;
          window.NotificationsUI?.updateBadge?.(0);
          lastUnreadIds = new Set();
          return;
        }

        const currentUnreadIds = new Set();
        let unreadCount = 0;

        data.forEach(n => {
          const el = document.createElement("button");
          el.className = `notif-item ${n.read ? "" : "unread"}`;
          el.type = "button";
          el.dataset.id = n.id;

          if (!n.read) {
            unreadCount++;
            currentUnreadIds.add(String(n.id));
          }

          const t = mapNotificationText(n);

          // ถ้า unread ให้มี span.dot นำหน้า
          const dotHTML = n.read ? '' : '<span class="dot"></span>';
          const createdAtText = formatThaiDateTime(n.createdAt);

          el.innerHTML = `
            <div class="title">
              ${dotHTML}${t.titleTH}
            </div>

            <!-- บรรทัดที่ 1: ข้อความหลัก -->
            <div class="meta">
              ${t.messageTH}
            </div>

            <!-- บรรทัดล่างสุด: วันที่ + time ago -->
            <div class="meta meta-time">
              ส่งเมื่อ ${createdAtText} · ${timeAgo(n.createdAt)}
            </div>
          `;


          notifList.appendChild(el);
        });

        // อัปเดต badge (ถ้า NotificationsUI รองรับส่งจำนวน)
        if (window.NotificationsUI?.updateBadge) {
          window.NotificationsUI.updateBadge(unreadCount);
        }

        // เช็คว่ามี noti ใหม่โผล่มาเมื่อเทียบกับรอบที่แล้วไหม
        let hasNewUnread = false;
        if (isAuto) {
          for (const id of currentUnreadIds) {
            if (!lastUnreadIds.has(id)) {
              hasNewUnread = true;
              break;
            }
          }
        }
        lastUnreadIds = currentUnreadIds;

        // ถ้าเป็น auto-refresh และมี noti ใหม่ → ให้ "เด้ง" ขึ้นมาอัตโนมัติ
        if (hasNewUnread) {
          console.log("📢 New notifications arrived!");

          // ถ้ามีฟังก์ชันเปิด panel ใน NotificationsUI
          if (window.NotificationsUI?.openPanel) {
            window.NotificationsUI.openPanel();
          } else {
            // หรือใช้ class / event ตามที่คุณใช้ในไฟล์ notifications.js
            notifPanel.classList.add("open");

            // หรือยิง event ให้ไฟล์อื่นจัดการ UI
            document.dispatchEvent(new CustomEvent("notification:new", {
              detail: { unreadCount }
            }));
          }
        }

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

    // ✅ โหลดตอนเปิดหน้า
    loadNotifications(false);

    // ✅ ตั้งให้ refresh อัตโนมัติทุก 30 วินาที (จะเด้ง panel ถ้ามี noti ใหม่)
    setInterval(() => {
      loadNotifications(true);
    }, 30_000);

    function formatThaiDateTime(iso) {
      if (!iso) return "-";
      const d = new Date(iso);

      const day = String(d.getDate()).padStart(2, "0");
      const month = String(d.getMonth() + 1).padStart(2, "0");
      const year = d.getFullYear() + 543; // แปลงเป็น พ.ศ.

      const hh = String(d.getHours()).padStart(2, "0");
      const mm = String(d.getMinutes()).padStart(2, "0");

      return `${day}/${month}/${year} ${hh}:${mm} น.`;
    }

  })();
});
