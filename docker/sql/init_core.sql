:on error exit

-- 0) Create DB if needed
IF DB_ID('bookingDB') IS NULL
BEGIN
  PRINT 'Creating database [bookingDB]...';
  CREATE DATABASE [bookingDB];
END
GO

-- 0.1) Wait until DB is ONLINE
DECLARE @i int = 0;
WHILE DB_ID('bookingDB') IS NULL 
   OR EXISTS (SELECT 1 FROM sys.databases WHERE name = 'bookingDB' AND state_desc <> 'ONLINE')
BEGIN
  SET @i += 1;
  PRINT CONCAT('Waiting bookingDB ONLINE... (', @i, ')');
  WAITFOR DELAY '00:00:01';
END
GO

-- 1) Login
USE [master];
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = 'booking_app')
BEGIN
  PRINT 'Creating login [booking_app]...';
  EXEC('CREATE LOGIN [booking_app] WITH PASSWORD = N''__APPPASS__'', CHECK_POLICY = OFF, CHECK_EXPIRATION = OFF;');
END
GO

-- 2) User & role
USE [bookingDB];
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'booking_app')
  CREATE USER [booking_app] FOR LOGIN [booking_app];
IF NOT EXISTS (
  SELECT 1
  FROM sys.database_role_members rm
  JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id AND r.name='db_owner'
  JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id AND u.name='booking_app'
)
  ALTER ROLE db_owner ADD MEMBER [booking_app];
GO

-- ===========================
-- rooms
-- ===========================
IF OBJECT_ID('dbo.rooms','U') IS NULL
BEGIN
  PRINT 'Creating table dbo.rooms';
  CREATE TABLE dbo.rooms(
    code          VARCHAR(20)   NOT NULL PRIMARY KEY,  -- ใช้ code เป็น PK
    room_name     NVARCHAR(200) NOT NULL,
    room_type     NVARCHAR(100) NOT NULL,
    min_capacity  INT           NOT NULL DEFAULT 1,
    max_capacity  INT           NOT NULL,
    features_json NVARCHAR(MAX) NULL,
    active        BIT           NOT NULL DEFAULT 1,
    CONSTRAINT ck_rooms_capacity
      CHECK (max_capacity >= min_capacity AND max_capacity > 0)
  );
END;

-- ===========================
-- Timeslot
-- ===========================
IF OBJECT_ID('dbo.time_slots','U') IS NULL
BEGIN
  PRINT 'Creating table dbo.time_slots';
  CREATE TABLE dbo.time_slots(
    slot_code  VARCHAR(20) NOT NULL PRIMARY KEY,  -- ใช้ slot_code เป็น PK
    start_time TIME        NOT NULL,
    end_time   TIME        NOT NULL
  );
END;

-- ===========================
-- reservations (Head)
-- ===========================
IF OBJECT_ID('dbo.reservations','U') IS NULL
BEGIN
  PRINT 'Creating table dbo.reservations';
  CREATE TABLE dbo.reservations(
    reservation_id       BIGINT IDENTITY(1,1) PRIMARY KEY,

    -- อ้างอิงด้วยรหัสห้อง (Natural Key)
    room_code            VARCHAR(20)     NOT NULL,
    reservation_date     DATE            NOT NULL,   -- วันของใบจอง (1 วันต่อใบ)

    -- ข้อมูลคำร้อง
    reason               NVARCHAR(255)   NULL,
    file_attachment      NVARCHAR(MAX)   NULL,

    -- สถานะขั้นตอน / ผลลัพธ์สุดท้าย
    step                 VARCHAR(30)     NULL,
    final_status         VARCHAR(20)     NULL,

    -- ผู้ยื่นคำร้อง
    user_email           VARCHAR(100)    NULL,
    user_name            NVARCHAR(100)   NULL,

    -- การตรวจสอบ/อนุมัติ (เก็บแบบ +07:00)
    staff_reviewer_email VARCHAR(100)    NULL,
    staff_reviewed_at    DATETIMEOFFSET(0) NULL,
    head_approver_email  VARCHAR(100)    NULL,
    head_decided_at      DATETIMEOFFSET(0) NULL,

    -- เหตุผลเฉพาะทาง
    return_reason        NVARCHAR(MAX)   NULL,
    reject_reason        NVARCHAR(MAX)   NULL,
    cancel_reason        NVARCHAR(MAX)   NULL,

    -- เวลาอนุมัติ/สร้าง (ไทย +07:00)
    approved_at          DATETIMEOFFSET(0) NULL,
    created_at           DATETIMEOFFSET(0) NOT NULL
      CONSTRAINT df_reservations_created_at
      DEFAULT ( (SYSUTCDATETIME() AT TIME ZONE 'UTC') AT TIME ZONE 'SE Asia Standard Time' ),

    CONSTRAINT fk_reservations_room_code
      FOREIGN KEY (room_code) REFERENCES dbo.rooms(code),

    -- จำกัดค่าที่อนุญาต (ตาม enum)
    CONSTRAINT ck_reservations_step
      CHECK (step IN ('SUBMITTED','STAFF_REVIEW','RETURNED_FOR_FIX','RESUBMITTED','HEAD_REVIEW','DECIDED') OR step IS NULL),

    CONSTRAINT ck_reservations_final_status
      CHECK (final_status IN ('PENDING','APPROVED','REJECTED','CANCELLED') OR final_status IS NULL)
  );

  -- ดัชนีสำหรับคัดกรองรอบแรกด้วยห้อง/วัน/สถานะ
  CREATE INDEX ix_reservations_room_date
    ON dbo.reservations(room_code, reservation_date, final_status);
END;
GO

-- ===========================
-- reservation_slots (Tail)
-- ===========================
IF OBJECT_ID('dbo.reservation_slots','U') IS NULL
BEGIN
  PRINT 'Creating table dbo.reservation_slots';
  CREATE TABLE dbo.reservation_slots(
    reservation_slot_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    reservation_id      BIGINT       NOT NULL,
    room_code           VARCHAR(20)  NOT NULL,   -- ซ้ำกับหัวเพื่อ join/query ตรงๆ
    slot_code           VARCHAR(20)  NOT NULL,   -- อ้างอิงช่วงเวลา (Natural Key)
    is_active           BIT          NOT NULL DEFAULT 1,

    CONSTRAINT fk_rs_reservation
      FOREIGN KEY (reservation_id) REFERENCES dbo.reservations(reservation_id),

    CONSTRAINT fk_rs_room_code
      FOREIGN KEY (room_code)      REFERENCES dbo.rooms(code),

    CONSTRAINT fk_rs_slot_code
      FOREIGN KEY (slot_code)      REFERENCES dbo.time_slots(slot_code)
  );

  -- ดัชนีสำหรับเช็คชนช่วงเวลาในใบที่คัดกรองมาแล้ว
  CREATE INDEX ix_reservation_slots_resv
    ON dbo.reservation_slots(reservation_id, slot_code) INCLUDE (is_active);

  -- ดัชนีช่วยค้นจากห้อง/ช่วงเวลา
  CREATE INDEX ix_reservation_slots_room_slot
    ON dbo.reservation_slots(room_code, slot_code)
    INCLUDE (reservation_id, is_active);
END;
GO

-- ===========================
-- user_reservation_logs
-- ===========================
IF OBJECT_ID('dbo.user_reservation_logs','U') IS NULL
BEGIN
  PRINT 'Creating table dbo.user_reservation_logs';
  CREATE TABLE dbo.user_reservation_logs(
    user_log_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    reservation_id  BIGINT           NOT NULL,
    user_email      VARCHAR(100)     NULL,
    action          VARCHAR(100)     NOT NULL,
    changed_at      DATETIMEOFFSET(0) NOT NULL
                     CONSTRAINT df_logs_changed_at
                     DEFAULT ( (SYSUTCDATETIME() AT TIME ZONE 'UTC') AT TIME ZONE 'SE Asia Standard Time' ),
    note            NVARCHAR(MAX)    NULL,
    CONSTRAINT fk_logs_reservation
      FOREIGN KEY(reservation_id) REFERENCES dbo.reservations(reservation_id),
    CONSTRAINT ck_logs_action CHECK (action IN ('CREATED','RESUBMITTED','CANCELED'))
  );

  CREATE INDEX idx_logs_reservation ON dbo.user_reservation_logs(reservation_id);
  CREATE INDEX idx_logs_user_email ON dbo.user_reservation_logs(user_email);
  CREATE INDEX idx_logs_changed_at ON dbo.user_reservation_logs(changed_at);
END;
GO

-- ===========================
-- staff_reservation_logs
-- ===========================
IF OBJECT_ID('dbo.staff_reservation_logs','U') IS NULL
BEGIN
  PRINT 'Creating table dbo.staff_reservation_logs';
  CREATE TABLE dbo.staff_reservation_logs (
    staff_log_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    reservation_id   BIGINT           NOT NULL,
    staff_email      VARCHAR(100)     NOT NULL,
    action           VARCHAR(50)      NOT NULL,
    note             NVARCHAR(MAX)    NULL,
    changed_at       DATETIMEOFFSET(0) NOT NULL
                      CONSTRAINT df_staff_changed_at
                      DEFAULT ( (SYSUTCDATETIME() AT TIME ZONE 'UTC') AT TIME ZONE 'SE Asia Standard Time' ),
    CONSTRAINT fk_stafflog_reservation FOREIGN KEY (reservation_id)
      REFERENCES dbo.reservations(reservation_id),
    CONSTRAINT ck_staff_action CHECK (action IN ('REVIEWED','RETURNED'))
  );

  CREATE INDEX idx_stafflog_reservation ON dbo.staff_reservation_logs(reservation_id);
  CREATE INDEX idx_stafflog_email ON dbo.staff_reservation_logs(staff_email);
  CREATE INDEX idx_stafflog_changed_at ON dbo.staff_reservation_logs(changed_at);
END;
GO

-- ===========================
-- notifications
-- ===========================
IF OBJECT_ID('dbo.notifications', 'U') IS NULL
BEGIN
    PRINT 'Creating table dbo.notifications';
    CREATE TABLE dbo.notifications (
        notification_id    BIGINT IDENTITY(1,1) PRIMARY KEY,

        recipient_email    VARCHAR(100)  NOT NULL,
        recipient_role     VARCHAR(30)   NOT NULL,   -- 'USER','STAFF','HEAD','ADMIN'

        reservation_id     BIGINT        NULL,
        user_log_id        BIGINT        NULL,

        notification_type  VARCHAR(50)   NOT NULL,   -- 'NEW_REQUEST', ...
        title              NVARCHAR(200) NULL,
        message            NVARCHAR(500) NOT NULL,

        channel            VARCHAR(20)   NOT NULL,   -- 'WEB','EMAIL'

        is_read            BIT           NOT NULL
                           CONSTRAINT DF_notifications_is_read DEFAULT (0),

        created_at         DATETIMEOFFSET(0) NOT NULL
                           CONSTRAINT DF_notifications_created_at
                           DEFAULT ( (SYSUTCDATETIME() AT TIME ZONE 'UTC') AT TIME ZONE 'SE Asia Standard Time' ),

        read_at            DATETIMEOFFSET(0) NULL,

        sent_at            DATETIMEOFFSET(0) NULL,
        send_status        VARCHAR(20)   NULL,       -- 'PENDING','SUCCESS','FAILED'
        send_error         NVARCHAR(500) NULL,

        is_deleted         BIT           NOT NULL
                           CONSTRAINT DF_notifications_is_deleted DEFAULT (0)
    );
END;
GO

-- index สำหรับกล่อง noti (web)
CREATE INDEX IX_notifications_inbox_web
ON dbo.notifications (
    recipient_email,
    recipient_role,
    channel,
    is_deleted,
    is_read,
    created_at DESC
);
GO

-- index สำหรับคิว email
CREATE INDEX IX_notifications_email_queue
ON dbo.notifications (
    channel,
    send_status,
    created_at
);
GO


