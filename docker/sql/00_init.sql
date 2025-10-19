-- สร้าง DB ถ้ายังไม่มี
IF DB_ID(N'bookingDB') IS NULL
BEGIN
  PRINT 'Creating DB bookingDB';
  CREATE DATABASE bookingDB;
END
GO

-- สร้าง LOGIN สำหรับแอป (ใช้รหัสจาก SPRING_DATASOURCE_PASSWORD ใน .env)
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = N'booking_app')
BEGIN
  PRINT 'Creating LOGIN booking_app';
  CREATE LOGIN booking_app WITH PASSWORD = 'Password1234';
END
GO

-- สร้าง USER ใน DB และให้สิทธิ์
USE bookingDB;
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'booking_app')
BEGIN
  PRINT 'Creating USER booking_app';
  CREATE USER booking_app FOR LOGIN booking_app;
  EXEC sp_addrolemember N'db_owner', N'booking_app'; -- เริ่มต้นให้สิทธิ์สูงเพื่อความง่าย (ลดทีหลังได้)
END
GO
PRINT 'Database initialization done.';