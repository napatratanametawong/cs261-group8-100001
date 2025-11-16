const NOTIFICATION_STORAGE_KEY = "header.notifications";
const STATUS_STATE_KEY = "header.notificationStatusState";
const HISTORY_ENDPOINT = "/api/me/reservations/history?page=0&size=5";
const POLL_INTERVAL_MS = 60 * 1000;

const hasStorage = typeof localStorage !== "undefined";

if (hasStorage) {
  startStatusSync();
}

function startStatusSync() {
  const run = () => {
    pollLatestReservations()
      .catch(() => { /* ignore errors but keep trying */ })
      .finally(() => {
        window.setTimeout(run, POLL_INTERVAL_MS);
      });
  };

  run();
}

async function pollLatestReservations() {
  const reservations = await fetchLatestReservations();
  if (!reservations || !reservations.length) return;

  const details = await Promise.allSettled(
    reservations.map((item) => fetchReservationDetail(item.reservationId))
  );

  const state = readStatusState();
  let updated = false;

  for (const result of details) {
    if (result.status !== "fulfilled") continue;
    const detail = result.value;
    if (!detail || !detail.reservationId) continue;
    const id = String(detail.reservationId);
    const prev = state[id] || {};
    const nextEntry = { ...prev };

    const events = [];

    const hasReturn = detail.step === "RETURNED_FOR_FIX" || Boolean(detail.returnReason);
    if (hasReturn && !prev.returnedNotified) {
      events.push(buildReturnMessage(detail));
      nextEntry.returnedNotified = true;
    }

    if (detail.staffReviewedAt && !prev.staffApprovedNotified) {
      events.push("อัพเดทสถานะ ขณะนี้เจ้าหน้าที่ดูแลอาคารตรวจสอบคำร้องของท่านสำเร็จ รอการอนุมัติจากหัวหน้าสาขาสักครู่");
      nextEntry.staffApprovedNotified = true;
    }

    if (detail.headDecidedAt && detail.finalStatus === "APPROVED" && !prev.headApprovedNotified) {
      events.push(buildHeadApprovedMessage(detail));
      nextEntry.headApprovedNotified = true;
    }

    if (detail.headDecidedAt && detail.finalStatus === "REJECTED" && !prev.headRejectedNotified) {
      events.push(buildHeadRejectedMessage(detail));
      nextEntry.headRejectedNotified = true;
    }

    if (events.length > 0) {
      events.forEach((message) => pushNotification(message));
      state[id] = nextEntry;
      updated = true;
    }
  }

  if (updated) {
    pruneAndPersistStatusState(state);
  }
}

async function fetchLatestReservations() {
  try {
    const res = await fetch(HISTORY_ENDPOINT, {
      method: "GET",
      credentials: "include",
      headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    return Array.isArray(data?.content) ? data.content : [];
  } catch {
    return [];
  }
}

async function fetchReservationDetail(id) {
  if (!id) return null;
  try {
    const res = await fetch(`/api/reservations/${id}`, {
      method: "GET",
      credentials: "include",
      headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch {
    return null;
  }
}

function getAuthHeaders() {
  const token =
    (typeof localStorage !== "undefined" && (localStorage.getItem("token") || localStorage.getItem("accessToken"))) ||
    (typeof sessionStorage !== "undefined" && (sessionStorage.getItem("token") || sessionStorage.getItem("accessToken")));
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function buildReturnMessage(detail) {
  const id = detail?.reservationId ? `หมายเลข: ${detail.reservationId}` : "หมายเลขไม่ทราบ";
  return `คำใช้สถานที่ ${id} ของท่านถูกตีกลับ คลิกเพื่อดูรายละเอียดเพิ่มเติม`;
}

function buildHeadApprovedMessage(detail) {
  const roomLabel = formatRoom(detail);
  const timeRange = formatSlotRange(detail);
  return `อัพเดทสถานะ ✅ ขณะนี้หัวหน้าสาขาได้อนุมัติคำขอของท่านเรียบร้อยแล้ว โดยสามารถใช้ห้อง : ${roomLabel} ได้ตั้งแต่เวลา : ${timeRange}`;
}

function buildHeadRejectedMessage(detail) {
  const roomLabel = formatRoom(detail);
  const timeRange = formatSlotRange(detail);
  return `อัพเดทสถานะ ❌ ขณะนี้หัวหน้าสาขาไม่อนุมัติคำขอของท่าน ห้อง : ${roomLabel} ได้ตั้งแต่เวลา : ${timeRange}`;
}

function formatRoom(detail) {
  return detail?.roomName || detail?.roomCode || "ห้องประชุม";
}

function formatSlotRange(detail) {
  const codes = [];
  if (Array.isArray(detail?.slots)) {
    detail.slots.forEach((slot) => {
      if (slot?.slotCode) codes.push(slot.slotCode);
    });
  }
  if (!codes.length && typeof detail?.slotCodes === "string") {
    detail.slotCodes
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean)
      .forEach((code) => codes.push(code));
  }
  if (!codes.length) return "-";

  let minStart = null;
  let maxEnd = null;

  codes.forEach((code) => {
    const match = /^S(\d{4})_(\d{4})$/.exec(code);
    if (!match) return;
    const start = match[1];
    const end = match[2];
    if (!minStart || start < minStart) minStart = start;
    if (!maxEnd || end > maxEnd) maxEnd = end;
  });

  const fmt = (value) => `${value.slice(0, 2)}:${value.slice(2)}`;
  return minStart && maxEnd ? `${fmt(minStart)} - ${fmt(maxEnd)}` : "-";
}

function readStatusState() {
  try {
    const raw = localStorage.getItem(STATUS_STATE_KEY);
    const parsed = raw ? JSON.parse(raw) : {};
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch {
    return {};
  }
}

function pruneAndPersistStatusState(state) {
  const entries = Object.entries(state)
    .sort((a, b) => Number(b[1]?.timestamp || 0) - Number(a[1]?.timestamp || 0));
  const limited = entries.slice(0, 50);
  const next = {};
  const now = Date.now();
  for (const [id, info] of limited) {
    next[id] = { ...info, timestamp: info?.timestamp || now };
  }
  try {
    localStorage.setItem(STATUS_STATE_KEY, JSON.stringify(next));
  } catch {
    /* ignore quota errors */
  }
}

function pushNotification(message) {
  if (!message) return;
  const notifications = readNotifications();
  const entry = {
    id: `status-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    message,
    createdAt: new Date().toISOString(),
    read: false,
  };
  const updated = [entry, ...notifications].slice(0, 30);
  try {
    localStorage.setItem(NOTIFICATION_STORAGE_KEY, JSON.stringify(updated));
    window.dispatchEvent(new Event("header:notifications:sync"));
  } catch {
    /* ignore storage errors */
  }
}

function readNotifications() {
  try {
    const raw = localStorage.getItem(NOTIFICATION_STORAGE_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}
