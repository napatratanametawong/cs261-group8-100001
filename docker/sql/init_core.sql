/* 0) Database */
IF DB_ID('bookingDB') IS NULL
BEGIN
  PRINT 'Creating database [bookingDB]...';
  CREATE DATABASE [bookingDB];
END
ELSE
  PRINT 'Database [bookingDB] already exists.';

/* 1) Login & User */
USE [master];
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = 'booking_app')
BEGIN
  PRINT 'Creating login [booking_app]...';
  EXEC('CREATE LOGIN [booking_app] WITH PASSWORD = N''__APPPASS__'', CHECK_POLICY = OFF, CHECK_EXPIRATION = OFF;');
END
ELSE
  PRINT 'Login [booking_app] already exists.';

USE [bookingDB];
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'booking_app')
BEGIN
  PRINT 'Creating user [booking_app]...';
  CREATE USER [booking_app] FOR LOGIN [booking_app];
END
ELSE
  PRINT 'User [booking_app] already exists.';

IF NOT EXISTS (
  SELECT 1
  FROM sys.database_role_members rm
  JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id AND r.name='db_owner'
  JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id AND u.name='booking_app'
)
BEGIN
  ALTER ROLE db_owner ADD MEMBER [booking_app];
END

/* 2) Tables (idempotent) */
-- rooms
IF OBJECT_ID('dbo.rooms','U') IS NULL BEGIN
  PRINT 'Creating table dbo.rooms';
  CREATE TABLE dbo.rooms(
    room_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    code          VARCHAR(20)   NOT NULL UNIQUE,
    room_name     NVARCHAR(200) NOT NULL,
    room_type     NVARCHAR(100) NOT NULL,
    min_capacity  INT           NOT NULL DEFAULT 1,
    max_capacity  INT           NOT NULL,
    features_json NVARCHAR(MAX) NULL,
    active        BIT           NOT NULL DEFAULT 1,
    CONSTRAINT ck_rooms_capacity CHECK (max_capacity >= min_capacity AND max_capacity > 0)
  );
  CREATE UNIQUE INDEX uk_rooms_code ON dbo.rooms(code);
END

-- time_slots
IF OBJECT_ID('dbo.time_slots','U') IS NULL BEGIN
  PRINT 'Creating table dbo.time_slots';
  CREATE TABLE dbo.time_slots(
    slot_id    BIGINT IDENTITY(1,1) PRIMARY KEY,
    slot_code  VARCHAR(20) NOT NULL UNIQUE,
    start_time TIME        NOT NULL,
    end_time   TIME        NOT NULL
  );
  CREATE UNIQUE INDEX uk_time_slots_code ON dbo.time_slots(slot_code);
END

-- reservations
IF OBJECT_ID('dbo.reservations','U') IS NULL BEGIN
  PRINT 'Creating table dbo.reservations';
  CREATE TABLE dbo.reservations(
    reservation_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    room_id   BIGINT NOT NULL,
    slot_id   BIGINT NOT NULL,
    [date]    DATE   NOT NULL,
    booked_by NVARCHAR(200) NULL,
    note      NVARCHAR(500) NULL,
    CONSTRAINT fk_res_room FOREIGN KEY(room_id) REFERENCES dbo.rooms(room_id),
    CONSTRAINT fk_res_slot FOREIGN KEY(slot_id) REFERENCES dbo.time_slots(slot_id),
    CONSTRAINT uk_res_unique UNIQUE(room_id, slot_id, [date])
  );
END
PRINT 'Database initialization script completed.';