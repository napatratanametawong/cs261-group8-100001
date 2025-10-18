-- Basic room status view for a given date
-- Note: This assumes no booking table yet; all active rooms are AVAILABLE.
-- The :date parameter is referenced to satisfy Hibernate named parameter binding.

SELECT 
  r.code        AS code,
  r.room_name   AS room_name,
  CASE WHEN r.active = 1 THEN 'AVAILABLE' ELSE 'INACTIVE' END AS status
FROM dbo.rooms r
WHERE 1 = 1
  AND CONVERT(date, :date) = CONVERT(date, :date)
ORDER BY r.code;

