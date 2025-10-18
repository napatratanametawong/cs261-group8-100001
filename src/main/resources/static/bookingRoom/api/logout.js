const API_BASE = location.origin; 
const LOGIN_PATH = "/login/pages/loginPage.html";
const LOGIN_URL = `${location.origin}${LOGIN_PATH}`;

function clearClientAuthState() {
  try { localStorage.removeItem("JWT_TOKEN"); } catch {}
  const span = document.getElementById("displayName");
  if (span) { span.textContent = "Guest"; delete span.dataset.role; }
}

/** เรียกใช้งานจากปุ่มหรือโค้ดอื่น ๆ ก็ได้ */
export async function logout() {
  const btn = document.getElementById("logout_btn");
  btn?.setAttribute("aria-busy", "true");
  if (btn) btn.disabled = true;

  try {
    await fetch(`${API_BASE}/auth/logout`, {
      method: "POST",
      credentials: "include",
      headers: { "Accept": "application/json" }
    });
  } catch (e) {
    console.warn("Logout request failed (network). Forcing client-side logout.", e);
  } finally {
    clearClientAuthState();
    // ป้องกันย้อนกลับมาเพจเดิม
    location.replace(LOGIN_URL);
    setTimeout(() => {
      if (location.href !== LOGIN_URL) window.location.href = LOGIN_URL;
    }, 150);
    btn?.removeAttribute("aria-busy");
    if (btn) btn.disabled = false;
  }
}

/** bind ปุ่มอัตโนมัติเมื่อ DOM พร้อม */
function bindLogoutButton() {
  const btn = document.getElementById("logout_btn");
  if (!btn) return;
  btn.addEventListener("click", (e) => {
    e.preventDefault();
    logout();
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", bindLogoutButton);
} else {
  bindLogoutButton();
}
