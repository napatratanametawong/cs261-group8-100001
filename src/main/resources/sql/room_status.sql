SELECT 
    r.room_name, 
    t.slot_code,
    CASE 
        WHEN res.reservation_id IS NULL THEN 'Available'
        ELSE 'Booked'
    END AS room_status
FROM rooms r
CROSS JOIN time_slots t
LEFT JOIN reservations res 
    ON r.room_id = res.room_id 
    AND t.slot_id = res.slot_id 
    AND res.date = :date
ORDER BY r.room_name, t.start_time;
