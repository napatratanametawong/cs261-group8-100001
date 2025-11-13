// Provides functions for cancelling a booking and rendering status pills.

export function statusPill(finalStatus) {
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

export function setupCancellationModal() {
    const modal = document.getElementById('cancel-modal');
    const modalReservationId = document.getElementById('modal-reservation-id');
    const closeBtn = document.getElementById('modal-btn-close');
    const confirmBtn = document.getElementById('modal-btn-confirm');

    if (!modal || !closeBtn || !confirmBtn) return;

    let currentReservationId = null;
    let currentItemElement = null;
    let currentApiConfig = {};

    const openModal = (reservationId, itemElement, apiConfig) => {
        currentReservationId = reservationId;
        currentItemElement = itemElement;
        currentApiConfig = apiConfig;
        modalReservationId.textContent = `#${reservationId}`;
        modal.classList.add('visible');
    };

    const closeModal = () => {
        modal.classList.remove('visible');
        confirmBtn.disabled = false;
        confirmBtn.textContent = 'ยืนยันการยกเลิก';
    };

    closeBtn.addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });

    confirmBtn.addEventListener('click', async () => {
        confirmBtn.disabled = true;
        confirmBtn.textContent = 'กำลังยกเลิก...';

        try {
            const res = await fetch(`${currentApiConfig.apiBase}/api/reservations/${currentReservationId}/cancel`, {
                method: 'PUT',
                headers: { "Content-Type": "application/json", ...currentApiConfig.authHeaders }
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({ message: `HTTP ${res.status}` }));
                throw new Error(errData.message);
            }

            // Success: update UI
            const statusPillEl = currentItemElement.querySelector('.status-pill');
            if (statusPillEl) statusPillEl.outerHTML = statusPill('CANCELLED');
            const originalCancelBtn = currentItemElement.querySelector(`[data-cancel="${currentReservationId}"]`);
            if (originalCancelBtn) originalCancelBtn.remove();
            closeModal();
        } catch (err) {
            alert(`เกิดข้อผิดพลาด: ${err.message}`); // Show error in a simple alert for now
            closeModal();
        }
    });

    // Expose the openModal function to be called from other scripts
    window.bookingHistoryAPI = { openCancelModal: openModal };
}