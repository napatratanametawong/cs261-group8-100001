document.addEventListener("DOMContentLoaded", function() {

    const titleScroller = document.getElementById("main-title-clickable");
    const historyTable = document.getElementById("history-table");

    if (titleScroller && historyTable) {
        titleScroller.addEventListener("click", () => {
            historyTable.scrollIntoView({ 
                behavior: 'smooth',
                block: 'start' 
            });
        });
    }

    const allItems = document.querySelectorAll(".history-item");

    allItems.forEach(item => {
        const summary = item.querySelector(".history-item-summary");
        const detail = item.querySelector(".history-item-detail");

        if (summary) {
            summary.addEventListener("click", () => {
                const isActive = item.classList.contains("active");

                if (isActive) {
                    item.classList.remove("active");
                    if (detail) {
                        detail.style.display = "none";
                    }
                } else {
                    item.classList.add("active");
                    if (detail) {
                        detail.style.display = "grid"; 
                    }
                }
            });
        }
    });

    const modal = document.getElementById("cancelModal");
    const cancelButtons = document.querySelectorAll(".btn-cancel");
    const modalBtnCancel = document.getElementById("modalBtnCancel");
    const modalBtnConfirm = document.getElementById("modalBtnConfirm");

    cancelButtons.forEach(button => {
        button.addEventListener("click", (event) => {
            event.stopPropagation();
            if (modal) {
                modal.style.display = "flex";
            }
        });
    });

    if (modalBtnCancel) {
        modalBtnCancel.addEventListener("click", () => {
            modal.style.display = "none";
        });
    }

    if (modalBtnConfirm) {
        modalBtnConfirm.addEventListener("click", () => {
            console.log("Cancelled");
            modal.style.display = "none";
        });
    }

    window.addEventListener("click", (event) => {
        if (event.target == modal) {
            modal.style.display = "none";
        }
    });

});

document.addEventListener("DOMContentLoaded", function() {
    
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

    const bellWrapper = document.getElementById("notification-bell-wrapper");
    const countBadge = document.getElementById("notification-count");
    const dropdown = document.getElementById("notification-dropdown");
    const listContainer = document.getElementById("notification-list-container");
    const detailPanel = document.getElementById("notification-detail-panel");
    const backBtn = document.getElementById("notification-back-btn");

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
        if (!listContainer) return;
        listContainer.innerHTML = "";
        let unreadCount = 0;

        mockNotifications.forEach(item => {
            if (!item.isRead) {
                unreadCount++;
            }
            listContainer.insertAdjacentHTML('beforeend', createNotificationHTML(item));
        });
        
        if (countBadge) {
            if (unreadCount > 0) {
                countBadge.textContent = unreadCount > 9 ? '9+' : unreadCount;
                countBadge.classList.add("show");
            } else {
                countBadge.classList.remove("show");
            }
        }
    }

    function showNotificationDetail(notification) {
        if (!detailPanel || !notification.detail) return;

        document.getElementById("notification-detail-title").textContent = notification.detail.title;
        document.getElementById("notification-detail-reason").textContent = notification.detail.reason;
        document.getElementById("notification-detail-note").textContent = notification.detail.note;
        document.getElementById("notification-detail-button").textContent = notification.detail.button;

        dropdown.classList.add("show-detail");
    }

    if (bellWrapper && dropdown && countBadge) {
        bellWrapper.addEventListener("click", function(event) {
            event.stopPropagation();
            dropdown.classList.toggle("show");
            dropdown.classList.remove("show-detail"); 
    
            if (dropdown.classList.contains("show")) {
                countBadge.classList.remove("show");
                
                const unreadItems = mockNotifications.filter(item => !item.isRead);
                unreadItems.forEach(item => item.isRead = true);
                
                document.querySelectorAll('.notification-item:not(.read) .dot').forEach(dot => {
                    dot.style.backgroundColor = 'transparent';
                });
            }
        });
    }

    if (listContainer) {
        listContainer.addEventListener("click", function(event) {
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

    if (backBtn && dropdown) {
        backBtn.addEventListener("click", function(event) {
            event.stopPropagation();
            dropdown.classList.remove("show-detail");
        });
    }

    window.addEventListener("click", function(event) {
        const modal = document.getElementById("cancelModal");
        if (event.target == modal) {
            return;
        }
        
        if (dropdown && dropdown.classList.contains("show")) {
            if (!bellWrapper.contains(event.target) && !dropdown.contains(event.target)) {
                dropdown.classList.remove("show");
                dropdown.classList.remove("show-detail");
            }
        }
    });

    loadNotifications();
});