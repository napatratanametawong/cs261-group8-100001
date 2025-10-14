IF OBJECT_ID('dbo.rooms','U') IS NULL EXEC(N'
  CREATE TABLE dbo.rooms(
    room_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    room_name NVARCHAR(200) NOT NULL,
    room_type NVARCHAR(100) NOT NULL,
    min_capacity INT NOT NULL DEFAULT 1,
    max_capacity INT NOT NULL,
    features_json NVARCHAR(MAX) NULL,
    active BIT NOT NULL DEFAULT 1,
    CONSTRAINT ck_rooms_capacity CHECK (max_capacity >= min_capacity AND max_capacity > 0)
  );
  CREATE UNIQUE INDEX uk_rooms_code ON dbo.rooms(code);
');
;
-- Floor: 1 --
-- Rooms(LC2-107)
UPDATE dbo.rooms SET room_name=N'ห้องปฏิบัติการคอมพิวเตอร์(Computer Lab)',room_type=N'Computer Lab',
  min_capacity=40,max_capacity=70,features_json=N'["Projector","Whiteboard","Computer"]',active=1
WHERE code='LC2-107';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-107',N'ห้องปฏิบัติการคอมพิวเตอร์(Computer Lab)',N'Computer Lab',40,70,N'["Projector","Whiteboard","Computer"]',1);
;

-- Rooms(LC2-111)
UPDATE dbo.rooms SET room_name=N'ห้องปฏิบัติการคอมพิวเตอร์(Computer Lab)',room_type=N'Computer Lab',
  min_capacity=30,max_capacity=50,features_json=N'["Projector","Whiteboard","Computer"]',active=1
WHERE code='LC2-111';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-111',N'ห้องปฏิบัติการคอมพิวเตอร์(Computer Lab)',N'Computer Lab',30,50,N'["Projector","Whiteboard","Computer"]',1);
;

-- Floor: 2 --
-- Rooms(LC2-213)
UPDATE dbo.rooms SET room_name=N'ห้องปฏิบัติการคอมพิวเตอร์(Computer Lab)',room_type=N'Computer Lab',
  min_capacity=10,max_capacity=100,features_json=N'["Projector","Whiteboard","Computer"]',active=1
WHERE code='LC2-213';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-213',N'ห้องปฏิบัติการคอมพิวเตอร์(Computer Lab)',N'Computer Lab',10,100,N'["Projector","Whiteboard","Computer"]',1);
;

-- Rooms(LC2-214)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม 1(Conference Room 1)',room_type=N'Meeting Room',
  min_capacity=10,max_capacity=30,features_json=N'["Whiteboard"]',active=1
WHERE code='LC2-214';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-214',N'ห้องประชุม 1(Conference Room 1)',N'Meeting Room',10,30,N'["Whiteboard"]',1);
;

-- Rooms(LC2-215)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม 2(Conference Room 2)',room_type=N'Meeting Room',
  min_capacity=10,max_capacity=30,features_json=N'["Whiteboard"]',active=1
WHERE code='LC2-215';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-215',N'ห้องประชุม 2(Conference Room 2)',N'Meeting Room',10,30,N'["Whiteboard"]',1);
;

-- Rooms(LC2-224)
UPDATE dbo.rooms SET room_name=N'ห้องเรียน(Lecture Room)',room_type=N'Lecture Room',
  min_capacity=10,max_capacity=50,features_json=N'["Projector","Whiteboard"]',active=1
WHERE code='LC2-224';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-224',N'ห้องเรียน(Lecture Room)',N'Lecture Room',10,50,N'["Projector","Whiteboard"]',1);
;

-- Rooms(LC2-226)
UPDATE dbo.rooms SET room_name=N'ห้องปฏิบัติการคอมพิวเตอร์ 1(Computer Lab 1)',room_type=N'Computer Lab',
  min_capacity=10,max_capacity=100,features_json=N'["Projector","Whiteboard","Computer"]',active=1
WHERE code='LC2-226';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-226',N'ห้องปฏิบัติการคอมพิวเตอร์ 1(Computer Lab 1)',N'Computer Lab',10,100,N'["Projector","Whiteboard","Computer"]',1);
;

-- Rooms(LC2-227)
UPDATE dbo.rooms SET room_name=N'ห้องสัมมนา (Seminar)',room_type=N'Lecture Room',
  min_capacity=10,max_capacity=60,features_json=N'["Projector"]',active=1
WHERE code='LC2-227';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-227',N'ห้องสัมมนา (Seminar)',N'Lecture Room',10,60,N'["Projector"]',1);
;

-- Rooms(LC2-228)
UPDATE dbo.rooms SET room_name=N'ห้องปฏิบัติการคอมพิวเตอร์ 2(Computer Lab 2)',room_type=N'Computer Lab',
  min_capacity=10,max_capacity=100,features_json=N'["Projector","Whiteboard","Computer"]',active=1
WHERE code='LC2-228';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-228',N'ห้องปฏิบัติการคอมพิวเตอร์ 2(Computer Lab 2)',N'Computer Lab',10,100,N'["Projector","Whiteboard","Computer"]',1);
;

-- Rooms(LC2-229)
UPDATE dbo.rooms SET room_name=N'ห้องปฏิบัติการคอมพิวเตอร์ 3(Computer Lab 3)',room_type=N'Computer Lab',
  min_capacity=10,max_capacity=100,features_json=N'["Projector","Whiteboard","Computer"]',active=1
WHERE code='LC2-229';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-229',N'ห้องปฏิบัติการคอมพิวเตอร์ 3(Computer Lab 3)',N'Computer Lab',10,100,N'["Projector","Whiteboard","Computer"]',1);
;

-- Rooms(LC2-230)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม(Conference Room)',room_type=N'Meeting Room',
  min_capacity=10,max_capacity=30,features_json=N'["Whiteboard"]',active=1
WHERE code='LC2-230';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-230',N'ห้องประชุม 2(Conference Room 2)',N'Meeting Room',10,30,N'["Whiteboard"]',1);
;

-- Floor: 3 --
-- Rooms(LC2-301)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม',room_type=N'Meeting Room',
  min_capacity=1,max_capacity=4,features_json=N'[]',active=1
WHERE code='LC2-301';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-301',N'ห้องประชุม',N'Meeting Room',1,4,N'[]',1);
;

-- Rooms(LC2-302)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม',room_type=N'Meeting Room',
  min_capacity=1,max_capacity=8,features_json=N'[]',active=1
WHERE code='LC2-302';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-302',N'ห้องประชุม',N'Meeting Room',1,8,N'[]',1);
;

-- Rooms(LC2-303)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม',room_type=N'Meeting Room',
  min_capacity=1,max_capacity=8,features_json=N'[]',active=1
WHERE code='LC2-303';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-303',N'ห้องประชุม',N'Meeting Room',1,8,N'[]',1);
;

-- Rooms(LC2-304)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม',room_type=N'Meeting Room',
  min_capacity=1,max_capacity=8,features_json=N'["Whiteboard"]',active=1
WHERE code='LC2-304';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-304',N'ห้องประชุม',N'Meeting Room',1,8,N'["Whiteboard"]',1);
;

-- Rooms(LC2-305)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม',room_type=N'Meeting Room',
  min_capacity=1,max_capacity=8,features_json=N'[]',active=1
WHERE code='LC2-305';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-305',N'ห้องประชุม',N'Meeting Room',1,8,N'[]',1);
;

-- Rooms(LC2-306)
UPDATE dbo.rooms SET room_name=N'ห้องเรียน',room_type=N'Lecture Room',
  min_capacity=30,max_capacity=50,features_json=N'["Projector","Whiteboard"]',active=1
WHERE code='LC2-306';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-306',N'ห้องเรียน',N'Lecture Room',30,50,N'["Projector","Whiteboard"]',1);
;

-- Rooms(LC2-308)
UPDATE dbo.rooms SET room_name=N'ห้องเรียน',room_type=N'Lecture Room',
  min_capacity=30,max_capacity=70,features_json=N'["Projector","Whiteboard"]',active=1
WHERE code='LC2-308';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-308',N'ห้องเรียน',N'Lecture Room',30,70,N'["เลื่อนที่กั้นเชื่อมกับห้อง 309" ,"Television"]',1);
;

-- Rooms(LC2-309)
UPDATE dbo.rooms SET room_name=N'ห้องเรียน',room_type=N'Lecture Room',
  min_capacity=30,max_capacity=70,features_json=N'["Projector","Whiteboard"]',active=1
WHERE code='LC2-309';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-309',N'ห้องเรียน',N'Lecture Room',30,70,N'["เลื่อนที่กั้นเชื่อมกับห้อง 310" ,"Television"]',1);
;

-- Rooms(LC2-310)
UPDATE dbo.rooms SET room_name=N'ห้องเรียน',room_type=N'Lecture Room',
  min_capacity=30,max_capacity=70,features_json=N'["Projector","Whiteboard"]',active=1
WHERE code='LC2-310';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-310',N'ห้องเรียน',N'Lecture Room',30,70,N'["เลื่อนที่กั้นเชื่อมกับห้อง 309" ,"Television"]',1);
;

-- Rooms(LC2-314)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม',room_type=N'Meeting Room',
  min_capacity=1,max_capacity=8,features_json=N'[]',active=1
WHERE code='LC2-314';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-314',N'ห้องประชุม',N'Meeting Room',1,8,N'[]',1);
;

-- Rooms(LC2-315)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม',room_type=N'Meeting Room',
  min_capacity=1,max_capacity=8,features_json=N'[]',active=1
WHERE code='LC2-315';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-315',N'ห้องประชุม',N'Meeting Room',1,8,N'[]',1);
;

-- Rooms(LC2-316)
UPDATE dbo.rooms SET room_name=N'ห้องประชุม',room_type=N'Meeting Room',
  min_capacity=1,max_capacity=8,features_json=N'[]',active=1
WHERE code='LC2-316';
IF @@ROWCOUNT=0
  INSERT INTO dbo.rooms(code,room_name,room_type,min_capacity,max_capacity,features_json,active)
  VALUES('LC2-316',N'ห้องประชุม',N'Meeting Room',1,8,N'[]',1);
;