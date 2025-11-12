// roomsApi.js – unified client for GET status and POST reservation
(function () {
  const firstSeg = location.pathname.split('/').filter(Boolean)[0] || '';
  const ctxFromPath = firstSeg ? `/${firstSeg}` : '';
  const API_BASE = ctxFromPath || '';
  const API_PREFIX = '/api/rooms';

  const ENDPOINTS = {
    status: (dateISO) => `${API_BASE}${API_PREFIX}/status?date=${encodeURIComponent(dateISO)}`,
    reservations: `${API_BASE}${API_PREFIX}/reservations`,
  };

  function getAuthHeaders() {
    const token =
      (typeof localStorage !== 'undefined' && (localStorage.getItem('token') || localStorage.getItem('accessToken'))) ||
      (typeof sessionStorage !== 'undefined' && (sessionStorage.getItem('token') || sessionStorage.getItem('accessToken')));
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  async function fetchJSON(url, init = {}) {
    const res = await fetch(url, {
      credentials: 'include',
      ...init,
      headers: { ...(init.headers || {}), ...getAuthHeaders() },
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const ct = res.headers.get('content-type') || '';
    return ct.includes('application/json') ? res.json() : null;
  }

  // GET /api/rooms/status?date=yyyy-MM-dd
  async function getRoomStatus(dateISO) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(String(dateISO))) {
      throw new Error('Invalid date format. Expect yyyy-MM-dd');
    }
    return fetchJSON(ENDPOINTS.status(dateISO));
  }

  function buildReservationForm(data) {
    if (data instanceof FormData) return data;
    const fd = new FormData();

    if (data.room_code) fd.set('room_code', data.room_code);
    if (data.room_name) fd.set('room_name', data.room_name);
    if (data.date) fd.set('date', data.date);

    if (Array.isArray(data.slot_code)) data.slot_code.forEach((s) => s && fd.append('slot_code', s));
    else if (data.slot_code) fd.append('slot_code', data.slot_code);

    if (data.user_name) fd.set('user_name', data.user_name);
    if (data.user_email) fd.set('user_email', data.user_email);
    if (data.phone_number) fd.set('phone_number', data.phone_number);
    if (data.reason) fd.set('reason', data.reason);

    const atts = data.attachments;
    if (atts) {
      const arr = Array.isArray(atts) ? atts : [atts];
      arr.forEach((f) => f && fd.append('attachments', f, f.name || undefined));
    }
    return fd;
  }

  // POST /api/rooms/reservations (multipart/form-data)
  async function createReservation(payload) {
    const body = buildReservationForm(payload);
    const res = await fetch(ENDPOINTS.reservations, {
      method: 'POST',
      body,
      credentials: 'include',
      headers: { ...getAuthHeaders() },
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    try { return await res.json(); } catch { return null; }
  }

  window.RoomsAPI = { getRoomStatus, createReservation, ENDPOINTS };
})();

