const BASE_URL = "http://localhost:8080";
const API_BASE = BASE_URL;

function safeTrim(s) {
    return typeof s === "string" ? s.trim() : "";
}

function pickDisplayName(me) {
    const emailStr = safeTrim(me?.email) || safeTrim(me?.profile?.email);
    const emailLocal = emailStr ? emailStr.split("@")[0] : "";

    return (
        safeTrim(me?.profile?.displayname_th) ||
        safeTrim(me?.profile?.userName) ||
        safeTrim(me?.username) ||
        emailLocal ||
        "ผู้ใช้"
    );
}

async function loadDisplayName() {
    const span = document.getElementById("displayName");
    if (!span) return;

    try {
        const res = await fetch(`${API_BASE}/auth/me`, {
            method: "GET",
            credentials: "include",              // ส่งคุกกี้ AUTH ไปด้วย
            headers: { "Accept": "application/json" }
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const me = await res.json();
        span.textContent = pickDisplayName(me);
        span.dataset.role = me.role || "";     // เผื่อเอาไปใช้ stylize ตาม role
    } catch (err) {
        console.warn("loadDisplayName failed:", err);
        span.textContent = "Guest";
        span.dataset.role = "";
    }
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", loadDisplayName);
} else {
    loadDisplayName();
}