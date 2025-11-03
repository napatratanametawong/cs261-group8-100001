------------------------------------------------------------
-- Table: reservations
------------------------------------------------------------
CREATE TABLE dbo.reservations (
    reservation_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    room_id            BIGINT       NOT NULL,
    reason             VARCHAR(255),
    file_attachment    TEXT,
    status             VARCHAR(50),               -- booking_step
    final_status       VARCHAR(50),               -- final_status ENUM
    user_email         VARCHAR(100),
    user_name          VARCHAR(100),
    phone_number       VARCHAR(15),
    staff_reviewer_email VARCHAR(100),
    staff_reviewed_at  DATETIME,
    head_approver_email VARCHAR(100),
    head_decided_at    DATETIME,
    return_reason      TEXT,
    reject_reason      TEXT,
    cancel_reason      TEXT,
    approved_at        DATETIME,
    created_at         DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_reservations_room
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(room_id)
);
GO

------------------------------------------------------------
-- Table: reservation_slots
------------------------------------------------------------
CREATE TABLE dbo.reservation_slots (
    reservation_slot_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    reservation_id      BIGINT NOT NULL,
    room_id             BIGINT NOT NULL,
    date                DATE   NOT NULL,
    slot_id             BIGINT NOT NULL,
    is_active           BIT    NOT NULL DEFAULT 1,

    CONSTRAINT FK_reservation_slots_reservation
        FOREIGN KEY (reservation_id) REFERENCES dbo.reservations(reservation_id) ON DELETE CASCADE,
    CONSTRAINT FK_reservation_slots_room
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(room_id),
    CONSTRAINT FK_reservation_slots_timeslot
        FOREIGN KEY (slot_id) REFERENCES dbo.time_slots(slot_id)
);
GO

------------------------------------------------------------
-- Indexes for performance / duplication prevention
------------------------------------------------------------
CREATE UNIQUE INDEX UX_reservation_slot_room_date_slot
ON dbo.reservation_slots (room_id, date, slot_id)
WHERE is_active = 1;
GO

------------------------------------------------------------
-- Table: reservation_slots
------------------------------------------------------------
CREATE TABLE dbo.reservation_slots (
    reservation_slot_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    reservation_id      BIGINT NOT NULL,
    room_id             BIGINT NOT NULL,
    date                DATE   NOT NULL,
    slot_id             BIGINT NOT NULL,
    is_active           BIT    NOT NULL DEFAULT 1,

    CONSTRAINT FK_reservation_slots_reservation
        FOREIGN KEY (reservation_id) REFERENCES dbo.reservations(reservation_id) ON DELETE CASCADE,
    CONSTRAINT FK_reservation_slots_room
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(room_id),
    CONSTRAINT FK_reservation_slots_timeslot
        FOREIGN KEY (slot_id) REFERENCES dbo.time_slots(slot_id)
);
GO

CREATE UNIQUE INDEX UX_reservation_slot_room_date_slot
  ON dbo.reservation_slots (room_id, date, slot_id)
  WHERE is_active = 1;
GO
