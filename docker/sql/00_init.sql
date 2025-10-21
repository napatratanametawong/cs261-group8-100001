SET NOCOUNT ON;
PRINT '== DB INIT START ==';

IF DB_ID(N'bookingDB') IS NULL
BEGIN
  PRINT 'Creating DB bookingDB';
  CREATE DATABASE bookingDB;
END
GO

USE bookingDB;
GO

IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = N'booking_app')
BEGIN
  PRINT 'Creating LOGIN booking_app';
  CREATE LOGIN [booking_app] WITH PASSWORD = N'Password1234!', CHECK_POLICY = ON, CHECK_EXPIRATION = OFF;
END
ELSE
BEGIN
  PRINT 'LOGIN booking_app exists, enabling + default DB';
  ALTER LOGIN [booking_app] ENABLE;
  ALTER LOGIN [booking_app] WITH DEFAULT_DATABASE = [bookingDB];
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'booking_app')
BEGIN
  PRINT 'Creating USER booking_app & grant db_owner (dev only)';
  CREATE USER [booking_app] FOR LOGIN [booking_app] WITH DEFAULT_SCHEMA = [dbo];
  EXEC sp_addrolemember N'db_owner', N'booking_app';
END
ELSE
BEGIN
  PRINT 'USER booking_app exists; mapping to LOGIN';
  ALTER USER [booking_app] WITH LOGIN = [booking_app];
END
GO

PRINT '== DB INIT DONE ==';
