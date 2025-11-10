document.addEventListener("DOMContentLoaded", function() {

    // 1. [อัปเดต] - ฟังก์ชันสำหรับดึง Token จริง
    // --------------------------------------------------
    function getAuthToken() {
        // สมมติว่าคุณเก็บ JWT Token ไว้ใน localStorage หลังล็อกอิน
        const token = localStorage.getItem('jwtToken');
        
        // หากไม่พบ Token จริง ให้ใช้ Token ทดสอบ (ควรลบออกเมื่อขึ้น Production)
        return token || "YOUR_JWT_TOKEN_HERE"; 
    }
    // --------------------------------------------------

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

    // 3. [อัปเดต] - สร้างตัวแปรเปล่าไว้เก็บการแจ้งเตือนที่ดึงมา
    let fetchedNotifications = [];

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
        if (!slotCodes || slotCodes.length === 0) return "-";
        
        let slotsArray = [];

        // API History (GET /api/me/reservations/history) ส่งมาเป็น String
        if (typeof slotCodes === 'string') {
            slotsArray = slotCodes.split(',').map(s => s.trim());
        } 
        // API Detail (GET /api/reservations/{id}) ส่งมาเป็น Array of Objects
        else if (Array.isArray(slotCodes)) {
             // ตรวจสอบว่ามีข้อมูลข้างในไหม
            if (slotCodes.length === 0 || !slotCodes[0].slotCode) return "-";
            slotsArray = slotCodes.map(s => s.slotCode);
        } else {
            return "-";
        }

        if (slotsArray.length === 0) return "-";

        const firstSlot = slotsArray[0].substring(1);
        const lastSlot = slotsArray[slotsArray.length - 1].substring(1);
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
            "ช่วงเวลา": formatSlots(data.slots), // API Detail ส่ง slots เป็น Array
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
            finalStep = { text: `อนุมัติคำร้อง (เมื่อ: ${formatDate(data.approvedAt)})`, status: "approved" };
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
        
        // อนุญาตให้ยกเลิกได้เฉพาะเมื่อสถานะยังเป็น PENDING
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
                    // 1. [อัปเดต] - เปลี่ยนไปใช้ getAuthToken()
                    'Authorization': `Bearer ${getAuthToken()}`,
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

    // 2. [อัปเดต] - เพิ่ม API Call สำหรับการยกเลิก
    // --------------------------------------------------
    if (modalBtnConfirm) {
        modalBtnConfirm.addEventListener("click", async () => {
            const itemId = modal.dataset.itemId;
            if (!itemId) return;

            modalBtnConfirm.textContent = 'กำลังดำเนินการ...';
            modalBtnConfirm.disabled = true;

            try {
                // สมมติว่า API สำหรับยกเลิกคือ: PUT /api/reservations/{id}/cancel
                const response = await fetch(`/api/reservations/${itemId}/cancel`, {
                    method: 'PUT',
                    headers: {
                        'Authorization': `Bearer ${getAuthToken()}`,
                        'Content-Type': 'application/json'
                    },
                    // body: JSON.stringify({ reason: "ยกเลิกโดยผู้ใช้" }) // (Optional) ถ้า API ต้องการเหตุผล
                });

                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({ message: 'ไม่สามารถยกเลิกได้' }));
                    throw new Error(errorData.message);
                }

                console.log(`Cancelled (API call for ${itemId} success)`);
                modal.style.display = "none";
                
                // โหลดข้อมูลประวัติใหม่เพื่ออัปเดตสถานะ
                loadHistoryData(); 

            } catch (error) {
                console.error('Error cancelling reservation:', error);
                alert(`เกิดข้อผิดพลาด: ${error.message}`);
            } finally {
                modalBtnConfirm.textContent = 'ยืนยัน';
                modalBtnConfirm.disabled = false;
            }
        });
    }
    // --------------------------------------------------

    async function loadHistoryData() {
        if (!historyListContainer) return;
        
        historyListContainer.innerHTML = "<p style='padding: 1.5rem;'>กำลังโหลดข้อมูล...</p>"; 

        try {
            const response = await fetch('/api/me/reservations/history?page=0&size=20', {
                method: 'GET',
                headers: {
                    // 1. [อัปเดต] - เปลี่ยนไปใช้ getAuthToken()
                    'Authorization': `Bearer ${getAuthToken()}`,
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
            
            if (!data.content || data.content.length === 0) {
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

    // 3. [ลบ] - ลบ mockNotifications ทั้งหมดทิ้ง
    /*
    const mockNotifications = [ ... ];
    */
    
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
        // API ควรส่ง 'read' (boolean) มาแทน 'isRead'
        const isReadClass = notification.read ? 'read' : '';
        const timeText = timeAgo(notification.timestamp); // API ควรส่ง 'timestamp'
        const hasDetailClass = notification.detail ? 'has-detail' : ''; // API ควรส่ง 'detail' (object)
        
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

    // 3. [อัปเดต] - เปลี่ยน `loadNotifications` ให้เป็น `async` และ `fetch` ข้อมูลจริง
    // --------------------------------------------------
    async function loadNotifications() {
        if (!notificationListContainer || !countBadge) return;

        notificationListContainer.innerHTML = "<div style='padding: 1rem; text-align: center;'>กำลังโหลด...</div>";
        
        try {
            // สมมติ API คือ GET /api/me/notifications
            const response = await fetch('/api/me/notifications?page=0&size=10&sort=timestamp,desc', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${getAuthToken()}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) throw new Error('ไม่สามารถโหลดการแจ้งเตือนได้');

            // สมมติ API ตอบกลับมาในรูปแบบเดียวกับ History (มี content และ unreadCount)
            const data = await response.json(); 
            
            // เก็บข้อมูลที่ดึงมาใส่ในตัวแปร global
            fetchedNotifications = data.content || []; 
            
            // สมมติ API มี field 'unreadCount' แยกมาให้
            const unreadCount = data.unreadCount || fetchedNotifications.filter(n => !n.read).length;

            notificationListContainer.innerHTML = ""; // Clear loading

            if (fetchedNotifications.length === 0) {
                notificationListContainer.innerHTML = "<div style='padding: 1rem; text-align: center;'>ไม่มีการแจ้งเตือน</div>";
            } else {
                fetchedNotifications.forEach(item => {
                    notificationListContainer.insertAdjacentHTML('beforeend', createNotificationHTML(item));
                });
            }
            
            if (unreadCount > 0) {
                countBadge.textContent = unreadCount > 9 ? '9+' : unreadCount;
                countBadge.classList.add("show");
            } else {
                countBadge.classList.remove("show");
            }

        } catch (error) {
            console.error('Error loading notifications:', error);
            notificationListContainer.innerHTML = `<div style='padding: 1rem; text-align: center; color: var(--color-red);'>${error.message}</div>`;
        }
    }
    // --------------------------------------------------

    function showNotificationDetail(notification) {
        if (!notificationDetailPanel || !notification.detail) return;

        // โครงสร้าง .detail นี้ ต้องตรงกับที่ API ส่งกลับมา
        document.getElementById("notification-detail-title").textContent = notification.detail.title;
        document.getElementById("notification-detail-reason").textContent = notification.detail.reason;
        document.getElementById("notification-detail-note").textContent = notification.detail.note;
        document.getElementById("notification-detail-button").textContent = notification.detail.button;

        notificationDropdown.classList.add("show-detail");
    }

    // 3. [อัปเดต] - เพิ่มฟังก์ชันสำหรับ Mark as Read
    // --------------------------------------------------
    async function markNotificationsAsRead() {
        // หากไม่มีการแจ้งเตือนที่ยังไม่อ่าน ก็ไม่ต้องยิง API
        if (!countBadge.classList.contains("show")) {
            return;
        }

        try {
            // สมมติ API คือ POST /api/me/notifications/mark-all-read
            const response = await fetch('/api/me/notifications/mark-all-read', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${getAuthToken()}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                console.log('Notifications marked as read');
                countBadge.classList.remove("show");
                // อัปเดต UI ให้อ่านแล้ว (ซ่อนจุด)
                document.querySelectorAll('.notification-item:not(.read) .dot').forEach(dot => {
                    dot.style.display = 'none';
                });
            }
        } catch (error) {
            console.error('Could not mark notifications as read:', error);
        }
    }
    // --------------------------------------------------

    if (bellWrapper && notificationDropdown && countBadge) {
        bellWrapper.addEventListener("click", function(event) {
            event.stopPropagation();
            const isShowing = notificationDropdown.classList.toggle("show");
            notificationDropdown.classList.remove("show-detail"); 
    
            if(userMenu) userMenu.classList.remove("show");

            if (isShowing) {
                // 3. [อัปเดต] - เมื่อเปิดกระดิ่ง ให้ยิง API เพื่อ Mark Read
                markNotificationsAsRead();
            }
        });
    }

    if (notificationListContainer) {
        notificationListContainer.addEventListener("click", function(event) {
            event.stopPropagation(); 
            const itemElement = event.target.closest('.notification-item');
            if (!itemElement) return;

            const notifId = parseInt(itemElement.dataset.id);
            
            // 3. [อัปเดต] - เปลี่ยนจาก `mockNotifications` เป็น `fetchedNotifications`
            const notification = fetchedNotifications.find(n => n.id === notifId);
            
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
            // ป้องกันการปิด Modal เมื่อคลิกที่พื้นหลัง (ถ้าต้องการให้ปิดได้ ให้ลบ return)
            // return; 
            modal.style.display = "none"; // (หากต้องการให้ปิดได้)
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

    // เรียกโหลดข้อมูลหลักเมื่อหน้าพร้อม
    loadHistoryData();
    loadNotifications();
});