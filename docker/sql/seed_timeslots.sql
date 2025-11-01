-- docker/sql/seed_timeslots.sql
USE [bookingDB];
SET NOCOUNT ON;

-- ถ้าตารางยังว่างอยู่ ให้ใส่ชุดค่าเริ่มต้น
IF NOT EXISTS (SELECT 1 FROM dbo.time_slots)
INSERT INTO dbo.time_slots (slot_code, start_time, end_time) VALUES
  ('S0800_0930', '08:00', '09:30'),
  ('S0930_1100', '09:30', '11:00'),
  ('S1100_1230', '11:00', '12:30'),
  ('S1330_1500', '13:30', '15:00'),
  ('S1500_1630', '15:00', '16:30'),
  ('S1630_1800', '16:30', '18:00');
