document.addEventListener("DOMContentLoaded", () => {
  const headerHTML = `
   <div class="top-bar">
    <div class="brand-logo">
      <img src="../../../resource/Logo_header.png" alt="Logo" height="56" />
    </div>
    <div class="toolbar">
      <button class="icon-btn" title="การแจ้งเตือน">
        <img src="../../../resource/bell.svg" alt="การแจ้งเตือน" />
      </button>

      <div class="profile-menu">
        <button class="chip profile-trigger" type="button">
          <span id="displayName" class="displayName"></span>
          <span class="caret">▾</span>
        </button>
        <div class="profile-dropdown">
          <a href="/bookingRoom/homepage_user.html"
             class="profile-item"
             id="profile-home-link">
            <span class="profile-icon">
              <!-- icon: home -->
              <img src="../../../resource/home.svg">
            </span>
            <span class="profile-label">หน้าแรก</span>
          </a>

          <a href="/bookingHistory/bookingHistory.html"
             class="profile-item"
             id="profile-history-link">
            <span class="profile-icon">
              <!-- icon: clock/history -->
              <img src="../../../resource/history.svg">
            </span>
            <span class="profile-label">ประวัติ</span>
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

  // ===== mark active menu by current URL (เปลี่ยนสีตัวอักษร + icon) =====
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

  // ====== profile dropdown: click เพื่อค้าง / click ข้างนอกเพื่อปิด ======
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

});
