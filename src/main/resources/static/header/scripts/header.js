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
        <span id="displayName" class="displayName">ณภัทร รัตนเมธาวงศ์</span>
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

   //ดึงปุ่ม logout แล้วเชื่อมกับ logout.js
  import("../../header-api/logout.js").then(module => {
    module.logout && document
      .getElementById("logout_btn")
      .addEventListener("click", module.logout);
  });
});
