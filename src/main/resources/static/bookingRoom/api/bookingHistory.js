document.addEventListener("DOMContentLoaded", function() {

    const dummyToken = "YOUR_JWT_TOKEN_HERE"; 

    const historyListContainer = document.querySelector(".history-list");
    const titleScroller = document.getElementById("main-title-clickable");
    const historyTable = document.getElementById("history-table");

    const modal = document.getElementById("cancelModal");
    const modalBtnCancel = document.getElementById("modalBtnCancel");
    const modalBtnConfirm = document.getElementById("modalBtnConfirm");

    const bellWrapper = document.getElementById("notification-bell-wrapper");
    const countBadge = document.getElementById("notification-count");
    const notificationDropdown = document.getElementById("notification-dropdown");
    const notificationListContainer = document.getElementById("notification-list-container");
    const notificationDetailPanel = document.getElementById("notification-detail-panel");
    const notificationBackBtn = document.getElementById("notification-back-btn");

    const userTrigger = document.getElementById("user-info-trigger");
    const userMenu = document.getElementById("user-menu-dropdown");

    function formatDate(dateString) {
        if (!dateString) return "-";
        try {
            const [year, month, day] = dateString.split('-');
            const buddhistYear = parseInt(year) + 543;
            return `${day}/${month}/${buddhistYear}`;
        } catch (e) {
            return dateString;
        }
    }

    function formatSlots(slotCodes) {
        if (!slotCodes || !Array.isArray(slotCodes) || slotCodes.length === 0) {
            if(typeof slotCodes === 'string') {
                 const slots = slotCodes.split(',').map(s => s.trim());
                 if (slots.length === 0) return "-";
                 const firstSlot = slots[0].substring(1);
                 const lastSlot = slots[slots.length - 1].substring(1);
                 const startTime = firstSlot.split('_')[0];
                 const endTime = lastSlot.split('_')[1];
                 return `${startTime.slice(0, 2)}:${startTime.slice(2)} - ${endTime.slice(0, 2)}:${endTime.slice(2)}`;
            }
            return "-";
        }
        
        const firstSlot = slotCodes[0].slotCode.substring(1);
        const lastSlot = slotCodes[slotCodes.length - 1].slotCode.substring(1);

        const startTime = firstSlot.split('_')[0];
        const endTime = lastSlot.split('_')[1];

        return `${startTime.slice(0, 2)}:${startTime.slice(2)} - ${endTime.slice(0, 2)}:${endTime.slice(2)}`;
    }
    
    function mapStatus(finalStatus) {
        switch (finalStatus) {
            case 'PENDING':
                return { class: "status-saved", text: "บันทึกแล้ว" };
            case 'APPROVED':
                return { class: "status-approved", text: "อนุมัติแล้ว" };
            case 'REJECTED':
                return { class: "status-rejected", text: "ไม่อนุมัติ" };
            case 'CANCELLED':
                return { class: "status-cancelled", text: "ยกเลิกแล้ว" };
            default:
                return { class: "status-grey", text: finalStatus || "ไม่ทราบสถานะ" };
        }
    }

    function createHistoryItemHTML(item) {
        const status = mapStatus(item.finalStatus);
        
        return `
        <div class="history-item" data-reservation-id="${item.reservationId}">
            <div class="history-item-summary">
                <div class="summary-col">${formatDate(item.reservationDate)}</div>
                <div class="summary-col">${item.reservationId}</div>
                <div class="summary-col">${item.roomCode}</div>
                <div class="summary-col">${formatSlots(item.slotCodes)}</div>
                <div class="summary-col"><span class="status-pill ${status.class}">${status.text}</span></div>
                <div class="summary-col"><i class="fa-solid fa-chevron-down chevron"></i></div>
            </div>
            
            <div class="history-item-detail">
                <div class="detail-info">
                    <p style="padding: 1rem;">กำลังโหลดรายละเอียด...</p>
                </div>
                <div class="detail-status">
                    <h2 class="main-detail">สถานะ</h2>
                    <p style="padding: 1rem;">กำลังโหลดสถานะ...</p>
                </div>
            </div>
        </div>
        `;
    }
    
    function parseDetails(data) {
        const detailsMap = {
            "ชื่อ-นามสกุล": data.userName,
            "อีเมล @dome": data.userEmail,
            "วันที่ต้องการใช้ห้อง": formatDate(data.reservationDate),
            "ช่วงเวลา": formatSlots(data.slots),
            "ห้อง": data.roomCode,
            "เหตุผลการจอง": data.reason || "-"
        };

        let detailHTML = '<h2 class="main-detail">รายละเอียดการยื่นคำร้อง</h2><div class="info-grid">';
        for (const [label, value] of Object.entries(detailsMap)) {
            detailHTML += `
                <div class="info-item">
                    <label>${label}</label>
                    <p>${value || '-'}</p>
                </div>
            `;
        }
        detailHTML += '</div>';
        return detailHTML;
    }

    function parseTimeline(data) {
        let steps = [];
        
        steps.push({ text: "บันทึกคำร้อง", status: (data.step === "SUBMITTED" && data.finalStatus === "PENDING") ? "active" : "completed" });

        steps.push({ 
            text: "เจ้าหน้าที่ตรวจสอบ", 
            status: data.step === "STAFF_REVIEW" ? "active" : (data.step === "HEAD_REVIEW" || data.step === "DECIDED") ? "completed" : "pending"
        });

        steps.push({
            text: "หัวหน้าสาขาฯ พิจารณา",
            status: data.step === "HEAD_REVIEW" ? "active" : data.step === "DECIDED" ? "completed" : "pending"
        });

        let finalStep = { text: "ผลการยื่นคำร้อง", status: "pending" };
        if (data.finalStatus === "APPROVED") {
            finalStep = { text: "อนุมัติคำร้อง", status: "approved" };
        } else if (data.finalStatus === "REJECTED") {
            finalStep = { text: `ไม่อนุมัติ (เหตุผล: ${data.rejectReason || 'N/A'})`, status: "rejected" };
        } else if (data.finalStatus === "CANCELLED") {
            finalStep = { text: `ยกเลิกคำร้อง (เหตุผล: ${data.cancelReason || 'N/A'})`, status: "cancelled" };
        }
        steps.push(finalStep);

        let timelineHTML = '<h2 class="main-detail">สถานะ</h2><div class="status-timeline">';
        steps.forEach(step => {
            timelineHTML += `<div class="status-step ${step.status}"><p>${step.text}</p></div>`;
        });
        timelineHTML += '</div>';
        
        if (data.finalStatus === 'PENDING') {
            timelineHTML += `<button class="btn-cancel" data-item-id="${data.reservationId}">ยกเลิกคำร้อง</button>`;
        }
        
        return timelineHTML;
    }
    
    async function loadHistoryDetail(itemElement, reservationId) {
        const detailInfo = itemElement.querySelector(".detail-info");
        const detailStatus = itemElement.querySelector(".detail-status");

        if (!detailInfo || !detailStatus) return;

        detailInfo.innerHTML = '<p style="padding: 1rem;">กำลังโหลดรายละเอียด...</p>';
        detailStatus.innerHTML = '<p style="padding: 1rem;">กำลังโหลดสถานะ...</p>';

        try {
            const response = await fetch(`/api/reservations/${reservationId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${dummyToken}`,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) throw new Error('ไม่สามารถดึงข้อมูลรายละเอียดได้');
            
            const data = await response.json();
            
            detailInfo.innerHTML = parseDetails(data);
            detailStatus.innerHTML = parseTimeline(data);
            
            itemElement.dataset.detailsLoaded = "true";
            
            const cancelButtons = detailStatus.querySelectorAll(".btn-cancel");
            cancelButtons.forEach(button => {
                button.addEventListener("click", (event) => {
                    event.stopPropagation();
                    if (modal) {
                        modal.dataset.itemId = event.target.dataset.itemId;
                        modal.style.display = "flex";
                    }
                });
            });

        } catch (error) {
            console.error('Error fetching detail:', error);
            detailInfo.innerHTML = `<p style='padding: 1rem; color:var(--color-red);'>${error.message}</p>`;
            detailStatus.innerHTML = "";
        }
    }

    function attachAccordionListeners() {
        const allItems = document.querySelectorAll(".history-item");

        allItems.forEach(item => {
            const summary = item.querySelector(".history-item-summary");
            const detail = item.querySelector(".history-item-detail");

            if (summary) {
                summary.addEventListener("click", () => {
                    const isActive = item.classList.contains("active");

                    if (isActive) {
                        item.classList.remove("active");
                        if (detail) detail.style.display = "none";
                    } else {
                        item.classList.add("active");
                        if (detail) detail.style.display = "grid";
                        
                        const isLoaded = item.dataset.detailsLoaded === "true";
                        if (!isLoaded) {
                            const reservationId = item.dataset.reservationId;
                            loadHistoryDetail(item, reservationId);
                        }
                    }
                });
            }
        });
    }

    if (modalBtnCancel) {
        modalBtnCancel.addEventListener("click", () => {
            modal.style.display = "none";
        });
    }

    if (modalBtnConfirm) {
        modalBtnConfirm.addEventListener("click", () => {
            const itemId = modal.dataset.itemId;
            console.log(`Cancelled (API call for ${itemId} goes here)`);
            modal.style.display = "none";
        });
    }

    async function loadHistoryData() {
        if (!historyListContainer) return;
        
        historyListContainer.innerHTML = "<p style='padding: 1.5rem;'>กำลังโหลดข้อมูล...</p>"; 

        try {
            const response = await fetch('/api/me/reservations/history?page=0&size=20', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${dummyToken}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                     throw new Error('Unauthorized. กรุณาตรวจสอบ Token.');
                }
                throw new Error('Network response was not ok');
            }
            
            const data = await response.json();
            
            historyListContainer.innerHTML = "";
            
            if (data.content.length === 0) {
                 historyListContainer.innerHTML = "<p style='padding: 1.5rem; text-align: center;'>ไม่พบประวัติการจอง</p>";
                 return;
            }
            
            data.content.forEach(item => {
                const itemHTML = createHistoryItemHTML(item);
                historyListContainer.insertAdjacentHTML('beforeend', itemHTML);
            });

            attachAccordionListeners();

        } catch (error) {
            console.error('เกิดข้อผิดพลาดในการดึงข้อมูล:', error);
            historyListContainer.innerHTML = `<p style='padding: 1.5rem; color:var(--color-red);'>เกิดข้อผิดพลาด: ${error.message}</p>`;
        }
    }

    if (titleScroller && historyTable) {
        titleScroller.addEventListener("click", () => {
            historyTable.scrollIntoView({ 
                behavior: 'smooth',
                block: 'start' 
            });
        });
    }

    const mockNotifications = [
        {
            id: 1,
            isRead: false,
            message: "คำร้องของท่านถูกตีกลับ กรุณาแก้ไขและส่งใหม่",
            timestamp: new Date(Date.now() - 5 * 60000).toISOString(),
            detail: {
                title: "คำร้องของท่านถูกตีกลับเนื่องจาก",
                reason: "XXXXXX",
                note: "ขออภัยในความไม่สะดวก",
                button: "แก้ไขคำร้อง"
            }
        },
        {
            id: 2,
            isRead: false,
            message: "มีคำร้องขอใช้สถานที่ใหม่ คลิกเพื่อดูรายละเอียด",
            timestamp: new Date(Date.now() - 12 * 3600000).toISOString(),
            detail: null
        },
        {
            id: 3,
            isRead: true,
            message: "คำร้องขอใช้สถานที่หมายเลข: 128397460 ได้รับการอนุมัติ",
            timestamp: "2025-09-14T10:00:00",
            detail: null
        },
        {
            id: 4,
            isRead: true,
            message: "คำร้องขอใช้สถานที่หมายเลข: 123452789 ได้ทำการ ยกเลิก",
            timestamp: "2025-03-12T17:00:00",
            detail: null
        },
        {
            id: 5,
            isRead: true,
            message: "คำร้องขอใช้สถานที่หมายเลข: 123456789 ทำการแก้ไขคำร้องแล้วส่งใหม่เรียบร้อย",
            timestamp: "2025-01-01T11:00:00",
            detail: null
        }
    ];

    function timeAgo(date) {
        const now = new Date();
        const seconds = Math.floor((now - new Date(date)) / 1000);
        let interval = seconds / 31536000;
        if (interval > 1) return Math.floor(interval) + " ปีที่แล้ว";
        interval = seconds / 2592000;
        if (interval > 1) return Math.floor(interval) + " เดือนที่แล้ว";
        interval = seconds / 86400;
        if (interval > 1) return Math.floor(interval) + " วันที่แล้ว";
        interval = seconds / 3600;
        if (interval > 1) return Math.floor(interval) + " ชั่วโมงที่แล้ว";
        interval = seconds / 60;
        if (interval > 1) return Math.floor(interval) + " นาทีที่แล้ว";
        return "เมื่อสักครู่";
    }

    function createNotificationHTML(notification) {
        const isReadClass = notification.isRead ? 'read' : '';
        const timeText = timeAgo(notification.timestamp);
        const hasDetailClass = notification.detail ? 'has-detail' : '';
        
        return `
            <div class="notification-item ${isReadClass} ${hasDetailClass}" data-id="${notification.id}">
                <div class="dot"></div>
                <div class="notification-item-content">
                    <p>${notification.message}</p>
                    <span>${timeText}</span>
                </div>
            </div>
        `;
    }

    function loadNotifications() {
        if (!notificationListContainer) return;
        notificationListContainer.innerHTML = "";
        let unreadCount = 0;

        mockNotifications.forEach(item => {
            if (!item.isRead) {
                unreadCount++;
            }
            notificationListContainer.insertAdjacentHTML('beforeend', createNotificationHTML(item));
        });
        
        if (countBadge) {
            if (countBadge > 0) {
                countBadge.textContent = unreadCount > 9 ? '9+' : unreadCount;
                countBadge.classList.add("show");
            } else {
                countBadge.classList.remove("show");
            }
        }
    }

    function showNotificationDetail(notification) {
        if (!notificationDetailPanel || !notification.detail) return;

        document.getElementById("notification-detail-title").textContent = notification.detail.title;
        document.getElementById("notification-detail-reason").textContent = notification.detail.reason;
        document.getElementById("notification-detail-note").textContent = notification.detail.note;
        document.getElementById("notification-detail-button").textContent = notification.detail.button;

        notificationDropdown.classList.add("show-detail");
    }

    if (bellWrapper && notificationDropdown && countBadge) {
        bellWrapper.addEventListener("click", function(event) {
            event.stopPropagation();
            const isShowing = notificationDropdown.classList.toggle("show");
            notificationDropdown.classList.remove("show-detail"); 
    
            if(userMenu) userMenu.classList.remove("show");

            if (isShowing) {
                countBadge.classList.remove("show");
                
                const unreadItems = mockNotifications.filter(item => !item.isRead);
                unreadItems.forEach(item => item.isRead = true);
                
                document.querySelectorAll('.notification-item:not(.read) .dot').forEach(dot => {
                    dot.style.backgroundColor = 'transparent';
                });
            }
        });
    }

    if (notificationListContainer) {
        notificationListContainer.addEventListener("click", function(event) {
            event.stopPropagation(); 
            const itemElement = event.target.closest('.notification-item');
            if (!itemElement) return;

            const notifId = parseInt(itemElement.dataset.id);
            const notification = mockNotifications.find(n => n.id === notifId);
            
            if (notification && notification.detail) {
                showNotificationDetail(notification);
            }
        });
    }

    if (notificationBackBtn && notificationDropdown) {
        notificationBackBtn.addEventListener("click", function(event) {
            event.stopPropagation();
            notificationDropdown.classList.remove("show-detail");
        });
    }

    if (userTrigger && userMenu) {
        userTrigger.addEventListener("click", function(event) {
            event.stopPropagation();
            userMenu.classList.toggle("show");
            if(notificationDropdown) notificationDropdown.classList.remove("show");
        });
    }
    
    window.addEventListener("click", function(event) {
        if (event.target == modal) {
            return;
        }
        
        if (notificationDropdown && notificationDropdown.classList.contains("show")) {
            if (!bellWrapper.contains(event.target) && !notificationDropdown.contains(event.target)) {
                notificationDropdown.classList.remove("show");
                notificationDropdown.classList.remove("show-detail");
            }
        }
        
        if (userMenu && userMenu.classList.contains("show")) {
            if (!userTrigger.contains(event.target) && !userMenu.contains(event.target)) {
                userMenu.classList.remove("show");
            }
        }
    });

    loadHistoryData();
    loadNotifications();
});