// ==================== CONFIG ====================
const BASE_API = ""; 
// ว่างไว้คือใช้ origin เดียวกับหน้าเว็บ เช่น http://localhost:8080
// ถ้า backend รันอีก port เช่น 8080, FE เปิดจาก file system ให้เขียนเป็น "http://localhost:8080"

// ==================== อ่าน reservation id จาก URL ====================
const params = new URLSearchParams(window.location.search);
const reservationId = params.get("id");

// ==================== DOM Elements พวกฟิลด์ข้อมูล ====================
const statusBox = document.getElementById("statusBox");
const titleEl = document.getElementById("reservationTitle");

const userNameEl = document.getElementById("userName");
const userEmailEl = document.getElementById("userEmail");
const reservationDateEl = document.getElementById("reservationDate");
const timeSlotEl = document.getElementById("timeSlot");
const roomTypeEl = document.getElementById("roomType");
const roomCodeEl = document.getElementById("roomCode");

const groupNameEl = document.getElementById("groupName");
const phoneNumberEl = document.getElementById("phoneNumber");
const reasonEl = document.getElementById("reason");
const fileAttachmentEl = document.getElementById("fileAttachment");

// ปุ่ม
const approveBtn = document.getElementById("approveBtn");
const rejectBtn = document.getElementById("rejectBtn");

// โมดัล + backdrop
const backdrop = document.getElementById("backdrop");
const approveModal = document.getElementById("approveModal");
const rejectModal = document.getElementById("rejectModal");
const approveConfirmBtn = document.getElementById("approveConfirmBtn");
const rejectConfirmBtn = document.getElementById("rejectConfirmBtn");
const rejectReason = document.getElementById("rejectReason");

let lastFocusedEl = null;
let currentReservation = null; // เก็บ object ที่โหลดมา

// ==================== Helper แสดงข้อความสถานะ ====================
function showStatus(message, type = "info") {
  if (!statusBox) return;
  statusBox.textContent = message;
  statusBox.className = "status-box " + type; // reset + เพิ่ม type
  statusBox.hidden = false;
}

function clearStatus() {
  if (!statusBox) return;
  statusBox.hidden = true;
  statusBox.textContent = "";
  statusBox.className = "status-box";
}

// ปิดการใช้งานปุ่มเมื่อใช้ไม่ได้
function disableDecisionButtons() {
  approveBtn.disabled = true;
  rejectBtn.disabled = true;
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

    const res = await fetch(`${BASE_API}/api/head-decide/${reservationId}`);

    if (!res.ok) {
      // backend อาจส่ง text หรือ json มาก็ได้ ลองอ่าน text ธรรมดา
      let msg = `โหลดข้อมูลไม่สำเร็จ (HTTP ${res.status})`;
      try {
        const text = await res.text();
        if (text) msg += `: ${text}`;
      } catch (e) {
        // ignore
      }
      showStatus(msg, "error");
      disableDecisionButtons();
      return;
    }

    const data = await res.json();
    currentReservation = data;
    renderReservation(data);
    validateReservationForDecision(data);

    showStatus("โหลดข้อมูลสำเร็จ", "success");
  } catch (err) {
    console.error(err);
    showStatus("เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์", "error");
    disableDecisionButtons();
  }
}

// เอาข้อมูลมาเติมในหน้า
function renderReservation(data) {
  // Title
  titleEl.textContent = `หมายเลขการจอง ${data.id}`;

  // ฝั่งซ้าย
  userNameEl.textContent = data.userName || "-";
  userEmailEl.textContent = data.userEmail || "-";

  // ถ้า reservationDate เป็น "2025-11-15" จะแสดงแบบเดิมก่อน
  reservationDateEl.textContent = data.reservationDate || "-";

  roomCodeEl.textContent = data.roomCode || "-";

  // เวลา / ประเภทห้อง / กลุ่ม / เบอร์ ในสเปกที่ให้มาไม่มี
  // ไว้ map เติมถ้า DTO มี field เพิ่ม (เช่น data.startTime, data.endTime, data.roomType ฯลฯ)
  // ตอนนี้ให้แสดง "-" ไปก่อนดีกว่า
  if (!timeSlotEl.dataset.bound) {
    timeSlotEl.textContent = "-";
  }
  if (!roomTypeEl.dataset.bound) {
    roomTypeEl.textContent = "-";
  }
  if (!groupNameEl.dataset.bound) {
    groupNameEl.textContent = "-";
  }
  if (!phoneNumberEl.dataset.bound) {
    phoneNumberEl.textContent = "-";
  }

  // จุดประสงค์ (ใน DTO ใช้ field "reason")
  reasonEl.textContent = data.reason || "-";

  // ไฟล์แนบ
  if (data.fileAttachment) {
    fileAttachmentEl.textContent = data.fileAttachment;
    // ถ้าในอนาคต backend ส่งเป็น URL ด้วย ค่อยเปลี่ยนเป็น <a> ที่นี่
  } else {
    fileAttachmentEl.textContent = "ไม่มีเอกสารแนบ";
  }
}

// เช็คว่าอยู่ใน step ที่อนุมัติได้ไหม
function validateReservationForDecision(data) {
  if (!data) return;

  const step = data.step;
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

  // ถ้าผ่านสองอันนี้ แสดงว่ายัง PENDING และอยู่ใน STAFF_REVIEW ให้ปุ่มใช้งานได้
  approveBtn.disabled = false;
  rejectBtn.disabled = false;
}

// ==================== Modal Logic เดิม ====================
function onEscToClose(e) {
  if (e.key === "Escape") closeAll();
}

function openModal(modalEl) {
  lastFocusedEl = document.activeElement;

  backdrop.hidden = false;
  modalEl.hidden = false;

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
  backdrop.classList.remove("show");
  approveModal.classList.remove("show");
  rejectModal.classList.remove("show");
  document.body.classList.remove("modal-open");
  document.removeEventListener("keydown", onEscToClose);

  const DURATION = 250;
  setTimeout(() => {
    backdrop.hidden = true;
    approveModal.hidden = true;
    rejectModal.hidden = true;
    lastFocusedEl?.focus();
  }, DURATION);
}

// ผูกปุ่มเปิดโมดัล
approveBtn.addEventListener("click", () => openModal(approveModal));
rejectBtn.addEventListener("click", () => {
  rejectReason.classList.remove("is-invalid");
  rejectReason.value = "";
  openModal(rejectModal);
});

// ==================== ฟังก์ชันยิง POST ตัดสินคำร้อง ====================
async function sendHeadDecision(decision, remark) {
  if (!reservationId) {
    showStatus("ไม่พบรหัสคำร้องใน URL (id)", "error");
    return;
  }

  const payload = {
    decision, // "APPROVED" หรือ "REJECTED"
    remark: remark || "",
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
      } catch (e) {
        // ignore
      }
      showStatus(msg, "error");
      return;
    }

    // ตามสเปก controller: 200 OK ไม่มี body
    showStatus("บันทึกผลการตัดสินเรียบร้อยแล้ว", "success");
    disableDecisionButtons();
  } catch (err) {
    console.error(err);
    showStatus("เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์", "error");
  }
}

// ==================== Confirm buttons events ====================
approveConfirmBtn.addEventListener("click", async () => {
  closeAll();
  await sendHeadDecision("APPROVED", "เห็นสมควรอนุมัติ"); // remark default
});

rejectConfirmBtn.addEventListener("click", async () => {
  const reason = rejectReason.value.trim();
  if (!reason) {
    rejectReason.classList.add("is-invalid");
    rejectReason.focus();
    return;
  }
  rejectReason.classList.remove("is-invalid");

  closeAll();
  await sendHeadDecision("REJECTED", reason);
});

// ==================== เริ่มทำงาน ====================
window.addEventListener("DOMContentLoaded", loadReservation);
