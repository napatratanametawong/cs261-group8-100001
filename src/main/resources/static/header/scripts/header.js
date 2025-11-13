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
      <div class="chip">
        <span id="displayName" class="displayName"></span>
      </div>
      <button class="icon-btn" id="logout_btn" title="ออกจากระบบ">
        <img src="../../../resource/logout.svg" alt="ออกจากระบบ" />
      </button>
    </div>
  </div>

  <header class="app"></header>
  `;

  // แทรก header เข้าไปที่ด้านบนสุดของ <body>
  document.body.insertAdjacentHTML("afterbegin", headerHTML);

// เชื่อม logout
  import("../../header-api/logout.js").then(module => {
    const btn = document.getElementById("logout_btn");
    btn?.addEventListener("click", (e) => {
      e.preventDefault();
      module.logout();
    });
  });

  // โหลดชื่อผู้ใช้
  import("../../header-api/profile.js").then(module => {
    module.loadDisplayName && module.loadDisplayName();
  });

});
