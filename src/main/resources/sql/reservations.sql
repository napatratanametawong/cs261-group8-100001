IF OBJECT_ID('dbo.reservations','U') IS NULL
EXEC(N'
  CREATE TABLE dbo.reservations (
    reservation_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    room_id BIGINT NOT NULL FOREIGN KEY REFERENCES dbo.rooms(room_id),
    slot_id BIGINT NOT NULL FOREIGN KEY REFERENCES dbo.time_slots(slot_id),
    [date] DATE NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uq_reservation UNIQUE (room_id, slot_id, [date])
  );
');
