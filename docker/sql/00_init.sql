/* สร้าง DB ถ้ายังไม่มี */
IF DB_ID(N'bookingDB') IS NULL
BEGIN
  CREATE DATABASE [bookingDB];
END
GO

/* สร้าง LOGIN ระดับเซิร์ฟเวอร์ถ้ายังไม่มี */
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = N'booking_app')
BEGIN
  CREATE LOGIN [booking_app]
    WITH PASSWORD = N'YourStrong@Passw0rd!',
         CHECK_POLICY = ON, CHECK_EXPIRATION = OFF;
END
ELSE
BEGIN
  ALTER LOGIN [booking_app] ENABLE;
END
GO

/* ผูก USER ใน bookingDB + ให้สิทธิ์ (dev: มี DDL ได้) */
USE [bookingDB];
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'booking_app')
  CREATE USER [booking_app] FOR LOGIN [booking_app] WITH DEFAULT_SCHEMA = [dbo];
ELSE
  ALTER USER [booking_app] WITH LOGIN = [booking_app];
GO

-- สิทธิ์ขั้นต่ำสำหรับให้แอป "สร้างตารางเองตอนรัน"
GRANT CONNECT TO [booking_app];
GRANT CREATE TABLE TO [booking_app];
GRANT ALTER ON SCHEMA::[dbo] TO [booking_app];

-- ถ้าจะให้แอป seed/อ่านเขียนด้วย
EXEC sp_addrolemember 'db_datareader', 'booking_app';
EXEC sp_addrolemember 'db_datawriter', 'booking_app';
GO
