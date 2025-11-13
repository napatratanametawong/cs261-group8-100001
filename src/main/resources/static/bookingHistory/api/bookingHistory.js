// /assets/js/history-enhanced.js
(() => {
    const API_BASE = "";
    const DEFAULT_PAGE_SIZE = 10;

    function getAuthHeaders() {
        const token =
            (localStorage.getItem("token") || localStorage.getItem("accessToken") ||
                sessionStorage.getItem("token") || sessionStorage.getItem("accessToken"));
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    // ===== Formatters =====
    function formatThaiDateYMD(ymd) {
        if (!ymd) return "-";
        const [y, m, d] = ymd.split("-").map(n => parseInt(n, 10));
        return `${String(d).padStart(2, "0")}/${String(m).padStart(2, "0")}/${y + 543}`;
    }

    // เพิ่ม: วันที่+เวลาแบบไทย (ใช้เฉพาะแสดง createdAt)
    function formatThaiDateTime(iso) {
        if (!iso) return "-";
        const [date, time] = iso.split("T");
        if (!date) return "-";
        const [y, m, d] = date.split("-").map(v => parseInt(v, 10));
        const hhmm = (time || "").slice(0, 5); // "HH:MM"
        return `${String(d).padStart(2, "0")}/${String(m).padStart(2, "0")}/${y + 543}${hhmm ? " " + hhmm : ""}`;
    }

    // "S1500_1630" -> "15:00–16:30"
    function slotCodeToLabel(code) {
        if (!/^S\d{4}_\d{4}$/.test(code || "")) return code || "-";
        const [a, b] = code.slice(1).split("_");
        const fmt = s => `${s.slice(0, 2)}:${s.slice(2)}`;
        return `${fmt(a)}–${fmt(b)}`;
    }

    // "S1500_1630, S1630_1800" -> "15:00–16:30, 16:30–18:00"
    function prettySlotCodes(slotCodes) {
        if (!slotCodes) return "-";
        return slotCodes
            .split(",")
            .map(s => s.trim())
            .filter(Boolean)
            .map(slotCodeToLabel)
            .join(", ");
    }

    // ===== สรุปช่วงเดียว (start แรก + end สุดท้าย) =====
    function slotCodesToSpan(slotCodes) {
        if (!slotCodes) return "-";
        const parts = slotCodes.split(",").map(s => s.trim()).filter(Boolean);
        let minStart = null, maxEnd = null;
        for (const p of parts) {
            const m = /^S(\d{4})_(\d{4})$/.exec(p);
            if (!m) continue;
            const [, s, e] = m;
            if (!minStart || s < minStart) minStart = s;
            if (!maxEnd || e > maxEnd) maxEnd = e;
        }
        const fmt = hhmm => `${hhmm.slice(0, 2)}:${hhmm.slice(2)}`;
        return (minStart && maxEnd) ? `${fmt(minStart)}–${fmt(maxEnd)}` : "-";
    }

    function compressPrettyTimeListToSpan(text) {
        if (!text) return "-";
        const re = /(\d{2}:\d{2})\s*[-–]\s*(\d{2}:\d{2})/g;
        let firstStart = null;
        let lastEnd = null;
        let m;
        while ((m = re.exec(text)) !== null) {
            if (!firstStart) firstStart = m[1];
            lastEnd = m[2];
        }
        return (firstStart && lastEnd) ? `${firstStart}–${lastEnd}` : text;
    }

    // ===== Status badge =====
    function statusPill(finalStatus) {
        const map = {
            PENDING: "status-processing",
            APPROVED: "status-approved",
            REJECTED: "status-rejected",
            CANCELLED: "status-cancelled",
            RETURNED: "status-not-saved"
        };
        const label = {
            PENDING: "กำลังพิจารณา",
            APPROVED: "อนุมัติแล้ว",
            REJECTED: "ถูกปฏิเสธ",
            CANCELLED: "ยกเลิก",
            RETURNED: "ส่งแก้ไข"
        }[finalStatus] || finalStatus || "-";
        const cls = map[finalStatus] || "status-processing";
        return `<span class="status-pill ${cls}">${label}</span>`;
    }

    // --- helper: ISO -> "dd/mm/BBBB HH:mm" (ไทย พ.ศ.)
    function formatThaiDateTimeShort(iso) {
        if (!iso) return "";
        const d = new Date(iso);
        const dd = String(d.getDate()).padStart(2, "0");
        const mm = String(d.getMonth() + 1).padStart(2, "0");
        const yyyy = d.getFullYear() + 543;
        const HH = String(d.getHours()).padStart(2, "0");
        const MM = String(d.getMinutes()).padStart(2, "0");
        return `${dd}/${mm}/${yyyy} ${HH}:${MM}`;
    }

    // ===== Timeline =====
    function buildTimeline(step, finalStatus, ts = {}) {
        const hasReturn = Boolean(ts.returnedAt || step === "RETURNED_FOR_FIX" || step === "RESUBMITTED");

        // ลำดับขั้นแบบพื้นฐาน และแทรก return/resubmit เมื่อจำเป็น
        const ORDER = hasReturn
            ? ["SUBMITTED", "STAFF_REVIEW", "RETURNED_FOR_FIX", "RESUBMITTED", "HEAD_REVIEW", "DECIDE"]
            : ["SUBMITTED", "STAFF_REVIEW", "HEAD_REVIEW", "DECIDE"];

        const LABELS = {
            SUBMITTED: "บันทึกคำร้อง",
            STAFF_REVIEW: "เจ้าหน้าที่ตรวจสอบ",
            RETURNED_FOR_FIX: "ส่งกลับแก้ไข",
            RESUBMITTED: "ส่งคำร้องใหม่",
            HEAD_REVIEW: "หัวหน้าสาขาพิจารณา",
            DECIDE: (finalStatus === "APPROVED") ? "อนุมัติแล้ว"
                : (finalStatus === "REJECTED") ? "ถูกปฏิเสธ"
                    : (finalStatus === "CANCELLED") ? "ยกเลิก"
                        : "ผลการยื่นคำร้อง"
        };

        // เวลาของแต่ละจุด (ใส่เท่าที่มี)
        const TIME = {
            SUBMITTED: ts.createdAt,
            STAFF_REVIEW: ts.staffReviewedAt,
            RETURNED_FOR_FIX: ts.returnedAt,
            RESUBMITTED: ts.resubmittedAt,
            HEAD_REVIEW: ts.headReviewedAt || ts.headDecidedAt,
            DECIDE: ts.approvedAt || ts.rejectedAt || ts.cancelledAt || ts.headDecidedAt
        };

        // ตำแหน่งปัจจุบัน
        const idxNow = Math.max(0, ORDER.indexOf(step ?? "SUBMITTED"));

        // กำหนดคลาสของจุด
        const classes = ORDER.map((k, i) => {
            if (k === "DECIDE") {
                if (finalStatus === "APPROVED") return "approved";
                if (finalStatus === "REJECTED") return "rejected";
                if (finalStatus === "CANCELLED") return "cancelled";
                return (idxNow >= ORDER.indexOf("DECIDE")) ? "active" : "pending";
            }
            if (finalStatus !== "PENDING") return "completed"; // จบแล้ว ทุกจุดก่อนหน้าเป็น completed
            if (i < idxNow) return "completed";
            if (i === idxNow) return "active";
            return "pending";
        });

        // เรนเดอร์แต่ละโหนด + note ตีกลับใต้ "บันทึกคำร้อง"
        return ORDER.map((k, i) => {
            const at = TIME[k];
            const label = LABELS[k];

            // note ใต้ "บันทึกคำร้อง" เฉพาะกรณีถูกตีกลับจริง
            const subNote =
                (k === "SUBMITTED" && hasReturn && ts.returnedAt)
                    ? `<div class="status-subnote">ถูกส่งกลับแก้ไข • ${formatThaiDateTimeShort(ts.returnedAt)}</div>`
                    : "";

            return `
    <div class="status-step ${classes[i]}">
      <div class="status-step__label"><strong>${label}</strong></div>
      <div class="status-step__meta">
        ${at ? `<div class="status-time">${formatThaiDateTimeShort(at)}</div>` : ""}
        ${subNote}
      </div>
    </div>
  `;
        }).join("");
    }


    // ===== Detail Panel =====
    function renderDetail(item, full) {
        const t = (v) => (v == null || v === "") ? "-" : v;

        const slotsSpan =
            Array.isArray(full?.slots) && full.slots.length
                ? slotCodesToSpan(full.slots.map(s => s.slotCode).join(","))
                : slotCodesToSpan(item.slotCodes);

        const slotsFullText =
            Array.isArray(full?.slots) && full.slots.length
                ? full.slots.map(s => `${slotCodeToLabel(s.slotCode)}${s.isActive ? "" : " (inactive)"}`).join(", ")
                : prettySlotCodes(item.slotCodes);

        const timeline = buildTimeline(item.step, item.finalStatus, {
            createdAt: full?.createdAt,
            staffReviewedAt: full?.staffReviewedAt,
            headDecidedAt: full?.headDecidedAt,
            approvedAt: full?.approvedAt,
            cancelledAt: full?.cancelReason ? item.lastStatusAt : null,
            rejectedAt: full?.rejectReason ? item.lastStatusAt : null,
        });

        const canCancel = item.finalStatus === "PENDING";

        return `
      <div class="history-item-detail">
        <div class="detail-info">
          <div class="main-detail">รายละเอียดการยื่นคำร้อง</div>
          <div class="info-grid">
            <div class="info-item">
              <label>ชื่อ–นามสกุล</label>
              <p>${t(full?.userName || item.userName)}</p>
            </div>
            <div class="info-item">
              <label>อีเมล</label>
              <p>${t(full?.userEmail || item.userEmail)}</p>
            </div>
            <div class="info-item">
              <label>หมายเลขคำร้อง</label>
              <p>#${item.reservationId}</p>
            </div>
            <div class="info-item">
              <label>วันที่ยื่นคำร้อง</label>
              <p>${formatThaiDateTime(full?.createdAt)}</p>
            </div>
            <div class="info-item">
              <label>วันที่ใช้ห้อง</label>
              <p>${formatThaiDateYMD(item.reservationDate)}</p>
            </div>
            <div class="info-item">
              <label>ช่วงเวลา</label>
              <p title="${slotsFullText || "-"}">${slotsSpan || "-"}</p>
            </div>
            <div class="info-item">
              <label>ห้อง</label>
              <p>${t(item.roomCode)}</p>
            </div>
            <div class="info-item">
              <label>เหตุผล/หมายเหตุ</label>
              <p>${t(full?.reason)}</p>
            </div>
          </div>
        </div>

        <div class="detail-status">
          <div class="main-detail">สถานะ</div>
          <div class="status-timeline">
            ${timeline}
          </div>
          ${canCancel ? `<div style="margin-top:1.25rem;">
              <button class="btn-cancel" data-cancel="${item.reservationId}">ยกเลิกคำร้อง</button>
            </div>` : ""}
        </div>
      </div>
    `;
    }

    // ===== Row (summary + detail collapsible) =====
    function renderItem(item) {
        const div = document.createElement("div");
        div.className = "history-item";
        div.innerHTML = `
  <div class="history-item-card">
    <div class="history-item-summary">
      <div>${formatThaiDateYMD(item.reservationDate)}</div>
      <div>#${item.reservationId}</div>
      <div>${item.roomCode || "-"}</div>
      <div title="${prettySlotCodes(item.slotCodes)}">${slotCodesToSpan(item.slotCodes)}</div>
      <div>${statusPill(item.finalStatus)}</div>
      <div class="chevron">
        <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M7 10l5 5 5-5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
    </div>
    <div class="history-item-detail"><!-- filled later --></div>
  </div>
`;

        const summary = div.querySelector(".history-item-summary");
        const detail = div.querySelector(".history-item-detail");

        let loaded = false;
        async function loadDetail() {
            if (loaded) return;
            try {
                const res = await fetch(`${API_BASE}/api/reservations/${encodeURIComponent(item.reservationId)}`, {
                    headers: { "Content-Type": "application/json", ...getAuthHeaders() }
                });
                const full = res.ok ? await res.json() : null;
                detail.outerHTML = renderDetail(item, full || {});
            } catch {
                detail.outerHTML = renderDetail(item, {});
            } finally {
                loaded = true;
                const btn = div.querySelector(`[data-cancel="${item.reservationId}"]`);
                if (btn) btn.addEventListener("click", () => handleCancel(item.reservationId, div));
            }
        }

        summary.addEventListener("click", async () => {
            const open = !div.classList.contains("active");
            document.querySelectorAll(".history-item.active").forEach(el => {
                el.classList.remove("active");
                const d = el.querySelector(".history-item-detail");
                if (d) d.style.display = "none";
            });
            if (open) {
                await loadDetail();
                div.classList.add("active");
                const d = div.querySelector(".history-item-detail");
                if (d) d.style.display = "grid";
            }
        });

        return div;
    }

    // ===== Pagination =====
    function renderPagination(page, totalPages, onGo) {
        const wrapper = document.querySelector(".history-table-wrapper"); // กล่องตารางหลัก
        if (!wrapper) return;

        let pager = document.getElementById("history-pagination"); // <nav id="history-pagination">
        if (!pager) {
            pager = document.createElement("nav");
            pager.id = "history-pagination";
            pager.className = "history-pagination";
        }

        pager.innerHTML = "";
        const mk = (label, idx, dis = false, act = false) => {
            const b = document.createElement("button");
            b.textContent = label;
            b.className = `pager-btn${act ? " active" : ""}`;
            b.disabled = dis;
            b.addEventListener("click", () => onGo(idx));
            return b;
        };

        pager.appendChild(mk("«", page - 1, page <= 0));
        for (let i = 0; i < totalPages; i++) pager.appendChild(mk(String(i + 1), i, false, i === page));
        pager.appendChild(mk("»", page + 1, page >= totalPages - 1));

        // วาง "ข้างนอก" กล่องตาราง: หลัง .history-table-wrapper
        if (!pager.isConnected) {
            wrapper.insertAdjacentElement("afterend", pager);
        }
    }

    // ===== Load list =====
    async function loadHistory(page = 0, size = DEFAULT_PAGE_SIZE) {
        const listHost = document.querySelector(".history-list");
        if (!listHost) return;
        listHost.innerHTML = `<div class="history-loading" style="padding:16px;text-align:center;">กำลังโหลด...</div>`;
        const qs = new URLSearchParams({ page: String(page), size: String(size) });

        try {
            const res = await fetch(`${API_BASE}/api/me/reservations/history?${qs}`, {
                headers: { "Content-Type": "application/json", ...getAuthHeaders() }
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();

            listHost.innerHTML = "";
            const items = Array.isArray(data.content) ? data.content : [];
            if (!items.length) {
                listHost.innerHTML = `<div style="padding:16px;text-align:center;">ยังไม่มีประวัติคำร้อง</div>`;
            } else {
                items.forEach(it => listHost.appendChild(renderItem(it)));
            }
            renderPagination(data.number ?? page, data.totalPages ?? 1, (go) => loadHistory(go, size));
        } catch (e) {
            listHost.innerHTML = `<div style="padding:16px;color:#c00;text-align:center;">โหลดไม่สำเร็จ: ${e.message || e}</div>`;
        }
    }

    document.addEventListener("DOMContentLoaded", () => loadHistory());
})();