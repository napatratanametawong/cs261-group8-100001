// ==================== CONFIG ====================
// ว่างไว้คือใช้ origin เดียวกับหน้าเว็บ เช่น http://localhost:8080
// ถ้า backend รันอีก port/host ให้แก้ตรงนี้
const BASE_API = "";

// ==================== อ่าน reservation id จาก URL ====================
const params = new URLSearchParams(window.location.search);
const reservationId = params.get("id");

// ==================== DOM Elements ====================
const statusBox         = document.getElementById("statusBox");
const titleEl           = document.getElementById("reservationTitle");

const userNameEl        = document.getElementById("userName");
const userEmailEl       = document.getElementById("userEmail");
const reservationDateEl = document.getElementById("reservationDate");
const timeSlotEl        = document.getElementById("timeSlot");
const roomTypeEl        = document.getElementById("roomType");
const roomCodeEl        = document.getElementById("roomCode");

const groupNameEl       = document.getElementById("groupName");
const phoneNumberEl     = document.getElementById("phoneNumber");
const reasonEl          = document.getElementById("reason");
const fileAttachmentEl  = document.getElementById("fileAttachment");

// ปุ่ม
const approveBtn        = document.getElementById("approveBtn");
const rejectBtn         = document.getElementById("rejectBtn");

// โมดัล + backdrop
const backdrop          = document.getElementById("backdrop");
const approveModal      = document.getElementById("approveModal");
const rejectModal       = document.getElementById("rejectModal");
const approveConfirmBtn = document.getElementById("approveConfirmBtn");
const rejectConfirmBtn  = document.getElementById("rejectConfirmBtn");
const rejectReason      = document.getElementById("rejectReason");

let lastFocusedEl       = null;
let currentReservation  = null; // เก็บ object ที่โหลดมา

// ==================== Helper: auth header (เหมือนหน้า history) ====================
function getAuthHeaders() {
  const token =
    localStorage.getItem("token")        ||
    localStorage.getItem("accessToken")  ||
    sessionStorage.getItem("token")      ||
    sessionStorage.getItem("accessToken");

  return token ? { Authorization: `Bearer ${token}` } : {};
}

// ==================== Helper แสดงข้อความสถานะ ====================
function showStatus(message, type = "info") {
  if (!statusBox) return;
  statusBox.textContent = message;
  statusBox.className   = "status-box " + type; // reset + เพิ่ม type
  statusBox.hidden      = false;
}

function clearStatus() {
  if (!statusBox) return;
  statusBox.hidden    = true;
  statusBox.textContent = "";
  statusBox.className = "status-box";
}

// ปิด / เปิด ปุ่ม
function disableDecisionButtons() {
  if (approveBtn) approveBtn.disabled = true;
  if (rejectBtn)  rejectBtn.disabled  = true;
}
function enableDecisionButtons() {
  if (approveBtn) approveBtn.disabled = false;
  if (rejectBtn)  rejectBtn.disabled  = false;
}

// ==================== helper: แปลง slotCodes -> "HH:MM–HH:MM" ====================
function slotCodesToSpan(slotCodes) {
  if (!slotCodes) return "-";
  const parts = String(slotCodes)
    .split(",")
    .map(s => s.trim())
    .filter(Boolean);

  let minStart = null;
  let maxEnd   = null;

  for (const p of parts) {
    const m = /^S(\d{4})_(\d{4})$/.exec(p);
    if (!m) continue;
    const [, s, e] = m;
    if (!minStart || s < minStart) minStart = s;
    if (!maxEnd   || e > maxEnd)   maxEnd   = e;
  }

  const fmt = hhmm => `${hhmm.slice(0, 2)}:${hhmm.slice(2)}`;
  return (minStart && maxEnd) ? `${fmt(minStart)}–${fmt(maxEnd)}` : "-";
}

// ==================== โหลดข้อมูลคำร้อง ====================
async function loadReservation() {
  if (!reservationId) {
    showStatus("ไม่พบรหัสคำร้องใน URL (พารามิเตอร์ id)", "error");
    disableDecisionButtons();
    return;
  }

  try {
    clearStatus();
    showStatus("กำลังโหลดข้อมูลคำร้อง...", "info");

    const headUrl = `${BASE_API}/api/head-decide/${reservationId}`;
    const fullUrl = `${BASE_API}/api/reservations/${reservationId}`;

    // เรียก 2 API พร้อมกัน
    const [headRes, fullRes] = await Promise.allSettled([
      fetch(headUrl),
      fetch(fullUrl, {
        headers: {
          "Content-Type": "application/json",
          ...getAuthHeaders(),
        },
      }),
    ]);

    // ---- handle /api/head-decide/{id} (ตัวหลัก) ----
    if (headRes.status !== "fulfilled" || !headRes.value.ok) {
      let msg = "โหลดข้อมูลไม่สำเร็จจาก /api/head-decide";
      try {
        const t = await headRes.value.text();
        if (t) msg += `: ${t}`;
      } catch {}
      showStatus(msg, "error");
      disableDecisionButtons();
      return;
    }
    const headData = await headRes.value.json();
    currentReservation = headData;

    // ---- /api/reservations/{id} (เสริม: เวลาจริง / ประเภทห้อง / กลุ่ม / เบอร์) ----
    let fullData = null;
    if (fullRes.status === "fulfilled" && fullRes.value.ok) {
      fullData = await fullRes.value.json();
      console.log("full reservation detail", fullData);
    }

    renderReservation(headData, fullData);
    validateReservationForDecision(headData);

    showStatus("โหลดข้อมูลสำเร็จ", "success");
  } catch (err) {
    console.error(err);
    showStatus("เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์", "error");
    disableDecisionButtons();
  }
}

// ==================== เอาข้อมูลมาเติมในหน้า ====================
function renderReservation(head, full) {
  if (!head) return;

  // Title
  if (titleEl)
    titleEl.textContent = `หมายเลขการจอง ${head.id ?? "-"}`;

  // -------- ฝั่งซ้าย: ใช้ของจาก HeadDecisionView --------
  if (userNameEl)        userNameEl.textContent        = head.userName        || "-";
  if (userEmailEl)       userEmailEl.textContent       = head.userEmail       || "-";
  if (reservationDateEl) reservationDateEl.textContent = head.reservationDate || "-";
  if (roomCodeEl)        roomCodeEl.textContent        = head.roomCode        || "-";

  // -------- เวลา / ประเภทห้อง จาก reservation detail --------
  if (timeSlotEl) {
    if (full && (full.slotCodes || (Array.isArray(full.slots) && full.slots.length))) {
      let slotCodes = full.slotCodes;
      if (!slotCodes && Array.isArray(full.slots)) {
        slotCodes = full.slots.map(s => s.slotCode).join(", ");
      }
      timeSlotEl.textContent = slotCodesToSpan(slotCodes);
    } else {
      timeSlotEl.textContent = "-";
    }
  }

  if (roomTypeEl) {
  const roomType =
    full?.roomType ||
    full?.roomTypeName ||
    full?.roomCategory ||
    full?.roomCategoryName ||
    full?.type ||
    full?.category ||
    (full?.room && (
      full.room.roomType ||
      full.room.roomTypeName ||
      full.room.category ||
      full.room.roomCategory
    ));

  roomTypeEl.textContent = roomType || "-";
}


  // -------- กลุ่ม / เบอร์ (ถ้า backend มี) --------
  if (groupNameEl)
    groupNameEl.textContent = full?.groupName || "-";

  if (phoneNumberEl)
    phoneNumberEl.textContent = full?.phoneNumber || "-";

  // -------- จุดประสงค์ --------
  if (reasonEl)
    reasonEl.textContent = (full && full.reason) || head.reason || "-";

  // -------- ไฟล์แนบ --------
  if (fileAttachmentEl) {
    if (head.fileAttachment) {
      fileAttachmentEl.textContent = head.fileAttachment;
    } else if (full && full.fileAttachment) {
      fileAttachmentEl.textContent = full.fileAttachment;
    } else {
      fileAttachmentEl.textContent = "ไม่มีเอกสารแนบ";
    }
  }
}

// ==================== เช็ค step ว่าตัดสินได้ไหม ====================
function validateReservationForDecision(data) {
  if (!data) return;

  const step        = data.step;
  const finalStatus = data.finalStatus; // PENDING / APPROVED / REJECTED / null

  if (step !== "STAFF_REVIEW") {
    showStatus(
      `คำร้องนี้ไม่อยู่ในขั้น STAFF_REVIEW (ปัจจุบัน: ${step || "-"}) จึงไม่สามารถตัดสินได้จากหน้านี้`,
      "error"
    );
    disableDecisionButtons();
    return;
  }

  if (finalStatus && finalStatus !== "PENDING") {
    showStatus(
      `คำร้องนี้ถูกตัดสินไปแล้ว (สถานะปัจจุบัน: ${finalStatus})`,
      "error"
    );
    disableDecisionButtons();
    return;
  }

  // ยัง PENDING และอยู่ใน STAFF_REVIEW
  enableDecisionButtons();
}

// ==================== Modal Logic ====================
function onEscToClose(e) {
  if (e.key === "Escape") closeAll();
}

function openModal(modalEl) {
  if (!modalEl || !backdrop) return;

  lastFocusedEl     = document.activeElement;
  backdrop.hidden   = false;
  modalEl.hidden    = false;

  requestAnimationFrame(() => {
    backdrop.classList.add("show");
    modalEl.classList.add("show");
    document.body.classList.add("modal-open");
  });

  const firstFocus =
    modalEl.querySelector("[data-close]") ||
    modalEl.querySelector("button, [href], textarea, input");
  firstFocus?.focus();

  document.addEventListener("keydown", onEscToClose);

  backdrop.addEventListener("click", closeAll, { once: true });
  modalEl
    .querySelectorAll("[data-close]")
    .forEach((b) => b.addEventListener("click", closeAll, { once: true }));
}

function closeAll() {
  if (!backdrop || !approveModal || !rejectModal) return;

  backdrop.classList.remove("show");
  approveModal.classList.remove("show");
  rejectModal.classList.remove("show");
  document.body.classList.remove("modal-open");
  document.removeEventListener("keydown", onEscToClose);

  const DURATION = 250;
  setTimeout(() => {
    backdrop.hidden     = true;
    approveModal.hidden = true;
    rejectModal.hidden  = true;
    lastFocusedEl?.focus();
  }, DURATION);
}

// ผูกปุ่มเปิดโมดัล
if (approveBtn) {
  approveBtn.addEventListener("click", () => openModal(approveModal));
}
if (rejectBtn) {
  rejectBtn.addEventListener("click", () => {
    if (rejectReason) {
      rejectReason.classList.remove("is-invalid");
      rejectReason.value = "";
    }
    openModal(rejectModal);
  });
}

// ==================== ฟังก์ชันยิง POST ตัดสินคำร้อง ====================
async function sendHeadDecision(decision, remark) {
  if (!reservationId) {
    showStatus("ไม่พบรหัสคำร้องใน URL (id)", "error");
    return;
  }

  const payload = {
    decision,          // "APPROVED" หรือ "REJECTED"
    remark: remark || ""
  };

  try {
    clearStatus();
    showStatus("กำลังบันทึกผลการตัดสิน...", "info");

    const res = await fetch(
      `${BASE_API}/api/head-decide/${reservationId}/decision`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      }
    );

    if (!res.ok) {
      let msg = `บันทึกผลไม่สำเร็จ (HTTP ${res.status})`;
      try {
        const text = await res.text();
        if (text) msg += `: ${text}`;
      } catch {}
      showStatus(msg, "error");
      return;
    }

     // เปลี่ยนข้อความตามผลการตัดสิน
    if (decision === "APPROVED") {
      showStatus("บันทึกผลการอนุมัติเรียบร้อยแล้ว", "success");
    } else if (decision === "REJECTED") {
      showStatus("ตีกลับคำร้องเรียบร้อยแล้ว", "success");
    } else {
      // กันเหนียว ถ้ามีค่าอื่นในอนาคต
      showStatus("บันทึกผลการตัดสินเรียบร้อยแล้ว", "success");
    }

    disableDecisionButtons();
  } catch (err) {
    console.error(err);
    showStatus("เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์", "error");
  }
}

// ==================== Confirm buttons events ====================
if (approveConfirmBtn) {
  approveConfirmBtn.addEventListener("click", async () => {
    closeAll();
    await sendHeadDecision("APPROVED", "เห็นสมควรอนุมัติ");
  });
}

if (rejectConfirmBtn) {
  rejectConfirmBtn.addEventListener("click", async () => {
    const reason = rejectReason?.value.trim() || "";
    if (!reason) {
      if (rejectReason) {
        rejectReason.classList.add("is-invalid");
        rejectReason.focus();
      }
      return;
    }
    if (rejectReason) rejectReason.classList.remove("is-invalid");

    closeAll();
    await sendHeadDecision("REJECTED", reason);
  });
}

// ==================== เริ่มทำงาน ====================
window.addEventListener("DOMContentLoaded", loadReservation);
