SELECT r.room_name, ts.slot_code,
       CASE 
           WHEN b.reservation_id IS NOT NULL THEN 'Booked'
           ELSE 'Available'
       END AS room_status
FROM rooms r
CROSS JOIN time_slots ts
LEFT JOIN reservations b 
   ON b.room_id = r.room_id 
   AND b.slot_id = ts.slot_id 
   AND CAST(b.date AS DATE) = CAST(:date AS DATE)
WHERE r.active = 1;


