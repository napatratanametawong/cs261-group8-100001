document.addEventListener("DOMContentLoaded", () => {
    const headerHTML = `
        <div class="top-bar">
            <div class="brand-logo">
                <img src="../resource/Logo_header.png" alt="Logo" height="56" />
            </div>

            <div class="toolbar">
                <button class="icon-btn" title="การแจ้งเตือน">
                    <img src="../resource/bell.svg" alt="การแจ้งเตือน" />
                </button>

                <div class="chip">
                    <span id="displayName" class="displayName"></span>
                </div>

                <button class="icon-btn" id="logout_btn" title="ออกจากระบบ">
                    <img src="../resource/logout.svg" alt="ออกจากระบบ" />
                </button>
        
            </div>
        </div>
`;

    document.body.insertAdjacentHTML("afterbegin", headerHTML);
});