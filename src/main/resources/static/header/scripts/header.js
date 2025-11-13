document.addEventListener("DOMContentLoaded", () => {
  const headerHTML = `
    <div class="top-bar">
      <div class="brand-logo">
        <img src="../resource/Logo_header.png" alt="Logo" />
      </div>
      <div class="toolbar">
        <button class="icon-btn" title="แจ้งเตือน">
          <img src="../resource/bell.svg" alt="แจ้งเตือน" />
        </button>
        <div class="chip">
          <span id="displayName" class="displayName">Guest</span>
        </div>
        <button class="icon-btn" id="logout_btn" title="ออก">
          <img src="../resource/logout.svg" alt="ออก" />
        </button>
      </div>
    </div>

});