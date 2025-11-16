document.addEventListener("DOMContentLoaded", () => {
  const STORAGE_KEY = "header.notifications";

  const headerHTML = `
   <div class="top-bar">
    <div class="brand-logo">
      <img src="../../../resource/Logo_header.png" alt="Logo" height="56" />
    </div>
    <div class="toolbar">
      <div class="notification-menu">
        <button class="icon-btn notification-trigger" type="button" title="การแจ้งเตือน">
          <img src="../../../resource/bell.svg" alt="การแจ้งเตือน" />
          <span class="notify-badge hidden" id="notificationBadge">0</span>
        </button>
        <div class="notification-dropdown">
          <div class="notification-head">
            <p class="notification-title">Notification</p>
            <button type="button" class="mark-all-read" id="notificationMarkAll">Mark all read</button>
          </div>
          <div class="notification-list" id="notificationList">
            <p class="notification-empty">ยังไม่มีการแจ้งเตือน</p>
          </div>
        </div>
      </div>

      <div class="profile-menu">
        <button class="chip profile-trigger" type="button">
          <span id="displayName" class="displayName"></span>
          <span class="caret">-</span>
        </button>
        <div class="profile-dropdown">
          <a href="/bookingRoom/homepage_user.html"
             class="profile-item"
             id="profile-home-link">
            <span class="profile-icon">
              <img src="../../../resource/home.svg">
            </span>
            <span class="profile-label">หน้าหลัก</span>
          </a>

          <a href="/bookingHistory/bookingHistory.html"
             class="profile-item"
             id="profile-history-link">
            <span class="profile-icon">
              <img src="../../../resource/history.svg">
            </span>
            <span class="profile-label">ประวัติการจอง</span>
          </a>
        </div>
      </div>

      <button class="icon-btn" id="logout_btn" title="ออกจากระบบ">
        <img src="../../../resource/logout.svg" alt="ออกจากระบบ" />
      </button>
    </div>
  </div>

  <header class="app"></header>
  `;

  document.body.insertAdjacentHTML("afterbegin", headerHTML);

  const notificationMenu = document.querySelector(".notification-menu");
  const notificationTrigger = document.querySelector(".notification-trigger");
  const notificationDropdown = document.querySelector(".notification-dropdown");
  const notificationList = document.getElementById("notificationList");
  const badgeEl = document.getElementById("notificationBadge");
  const markReadBtn = document.getElementById("notificationMarkAll");

  const supportsStorage = (() => {
    try {
      if (typeof localStorage === "undefined") return false;
      const testKey = "__header_test";
      localStorage.setItem(testKey, "1");
      localStorage.removeItem(testKey);
      return true;
    } catch {
      return false;
    }
  })();

  const state = {
    notifications: [],
    timers: new Map(),
  };

  const escapeHtml = (value) => {
    return String(value || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  };

  const isVisible = (notif) => {
    if (!notif) return false;
    if (!notif.visibleAt) return true;
    return Date.now() >= Number(notif.visibleAt);
  };

  const readStorage = () => {
    if (!supportsStorage) return [];
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      const parsed = raw ? JSON.parse(raw) : [];
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  };

  const writeStorage = (items) => {
    if (!supportsStorage) return;
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    } catch {
      /* ignore storage errors */
    }
  };

  const formatThaiDateTime = (value) => {
    try {
      if (!value) return "";
      return new Date(value).toLocaleString("th-TH", {
        day: "2-digit",
        month: "short",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return "";
    }
  };

  const formatRelativeTime = (value) => {
    try {
      if (!value) return "";
      const past = new Date(value).getTime();
      if (!Number.isFinite(past)) return "";
      const diffMs = Date.now() - past;
      if (diffMs < 0) return "";
      const diffMinutes = Math.floor(diffMs / 60000);
      if (diffMinutes < 1) return "เพิ่งส่งคำร้อง";
      if (diffMinutes < 60) return `เป็นเวลา ${diffMinutes} นาที`;
      const diffHours = Math.floor(diffMinutes / 60);
      if (diffHours < 24) return `เป็นเวลา ${diffHours} ชั่วโมง`;
      const diffDays = Math.floor(diffHours / 24);
      return `เป็นเวลา ${diffDays} วัน`;
    } catch {
      return "";
    }
  };

  const renderNotifications = () => {
    if (!notificationList) return;
    const visibleItems = state.notifications.filter(isVisible);
    if (visibleItems.length === 0) {
      notificationList.innerHTML = `<p class="notification-empty">ยังไม่มีการแจ้งเตือน</p>`;
    } else {
      const html = visibleItems
        .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
        .map((item) => {
          const createdAt = item.createdAt;
          const timeLabel = formatThaiDateTime(createdAt);
          const relativeLabel = formatRelativeTime(createdAt);
          const calendarBlock = timeLabel
            ? `<span class="time-meta"><span class="time-icon icon-calendar"></span>${timeLabel}</span>`
            : "";
          const clockBlock = relativeLabel
            ? `<span class="time-meta"><span class="time-icon icon-clock"></span>${relativeLabel}</span>`
            : "";
          const timeMeta = [calendarBlock, clockBlock]
            .filter(Boolean)
            .join('<span class="time-separator">&nbsp;&nbsp;&nbsp;</span>');
          const classes = ["notification-item"];
          if (!item.read) classes.push("unread");
          return `
            <div class="${classes.join(" ")}" data-notification-id="${item.id}">
              <p class="notification-message">${escapeHtml(item.message || "ท่านได้ส่งคำขอเรียบร้อยแล้ว โปรดรอการตอบกลับภายใน 3 วันทำการ")}</p>
              <span class="notification-time">${timeMeta}</span>
            </div>
          `;
        })
        .join("");
      notificationList.innerHTML = html;
    }
    updateBadge(visibleItems);
  };

  const updateBadge = (visibleItems) => {
    if (!badgeEl) return;
    const unreadCount = visibleItems.filter((item) => !item.read).length;
    if (unreadCount > 0) {
      badgeEl.textContent = unreadCount > 99 ? "99+" : String(unreadCount);
      badgeEl.classList.remove("hidden");
    } else {
      badgeEl.classList.add("hidden");
    }
    if (markReadBtn) {
      markReadBtn.disabled = unreadCount === 0;
      markReadBtn.classList.toggle("disabled", unreadCount === 0);
    }
  };

  const markAllAsRead = () => {
    let changed = false;
    const updated = state.notifications.map((item) => {
      if (!item.read) {
        changed = true;
        return { ...item, read: true };
      }
      return item;
    });
    if (changed) {
      state.notifications = updated;
      writeStorage(updated);
      renderNotifications();
    }
  };

  const refreshNotifications = () => {
    state.notifications = readStorage();
    renderNotifications();
    scheduleVisibility();
  };

  const scheduleVisibility = () => {
    if (typeof window === "undefined") return;
    state.notifications.forEach((item) => {
      const pendingTimer = state.timers.get(item.id);
      if (pendingTimer && isVisible(item)) {
        clearTimeout(pendingTimer);
        state.timers.delete(item.id);
      }
      if (!item.visibleAt || isVisible(item) || state.timers.has(item.id)) {
        return;
      }
      const delay = Math.max(0, Number(item.visibleAt) - Date.now());
      const timerId = window.setTimeout(() => {
        state.timers.delete(item.id);
        renderNotifications();
      }, delay);
      state.timers.set(item.id, timerId);
    });
  };

  if (notificationMenu && notificationTrigger) {
    notificationTrigger.addEventListener("click", (event) => {
      event.stopPropagation();
      notificationMenu.classList.toggle("open");
    });
    notificationDropdown?.addEventListener("click", (event) => {
      event.stopPropagation();
    });
    document.addEventListener("click", () => {
      notificationMenu.classList.remove("open");
    });
  }

  markReadBtn?.addEventListener("click", (event) => {
    event.preventDefault();
    markAllAsRead();
  });

  window.addEventListener("storage", (event) => {
    if (event.key === STORAGE_KEY) {
      refreshNotifications();
    }
  });
  window.addEventListener("header:notifications:sync", refreshNotifications);
  refreshNotifications();

  // ===== mark active menu by current URL =====
  const path = window.location.pathname || "";
  const homeLink = document.getElementById("profile-home-link");
  const historyLink = document.getElementById("profile-history-link");

  if (homeLink && (
      path.endsWith("/bookingRoom/homepage_user.html") ||
      path === "/bookingRoom" ||
      path === "/bookingRoom/"
    )) {
    homeLink.classList.add("active");
  }

  if (historyLink && path.startsWith("/bookingHistory")) {
    historyLink.classList.add("active");
  }

  // ===== profile dropdown =====
  const profileMenu = document.querySelector(".profile-menu");
  const profileTrigger = document.querySelector(".profile-trigger");
  const dropdown = document.querySelector(".profile-dropdown");

  if (profileMenu && profileTrigger) {
    profileTrigger.addEventListener("click", (e) => {
      e.stopPropagation();
      profileMenu.classList.toggle("open");
    });

    if (dropdown) {
      dropdown.addEventListener("click", (e) => {
        e.stopPropagation();
      });
    }

    document.addEventListener("click", () => {
      profileMenu.classList.remove("open");
    });
  }

  // ===== logout =====
  import("../../header-api/logout.js").then(module => {
    const btn = document.getElementById("logout_btn");
    btn?.addEventListener("click", (e) => {
      e.preventDefault();
      module.logout();
    });
  });

  // ===== load display name =====
  import("../../header-api/profile.js").then(module => {
    module.loadDisplayName && module.loadDisplayName();
  });

  import("./notification-status-sync.js").catch(() => {
    // optional sync module failed; ignore
  });
});
