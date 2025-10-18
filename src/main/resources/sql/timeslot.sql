IF OBJECT_ID('dbo.time_slots', 'U') IS NULL
CREATE TABLE dbo.time_slots (
  slot_id BIGINT IDENTITY(1,1) PRIMARY KEY,
  slot_code VARCHAR(20) NOT NULL UNIQUE,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL
);

-- ลบข้อมูลเก่าเพื่อความแน่นอน
DELETE FROM dbo.time_slots;

-- เพิ่มช่วงเวลา
INSERT INTO dbo.time_slots (slot_code, start_time, end_time)
VALUES 
  ('S0800_0930', '08:00', '09:30'),
  ('S0930_1100', '09:30', '11:00'),
  ('S1100_1230', '11:00', '12:30'),
  ('S1330_1500', '13:30', '15:00'),
  ('S1500_1630', '15:00', '16:30'),
  ('S1630_1800', '16:30', '18:00');
