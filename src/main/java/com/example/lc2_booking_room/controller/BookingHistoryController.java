package com.example.lc2_booking_room.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BookingHistoryController {

    @GetMapping("/booking-history")
    public List<Map<String, Object>> getBookingHistory() {
        return List.of(
            Map.of(
                "id", "123453609",
                "date", "04/08/2569",
                "room", "LC2-213",
                "time", "13:30-18:00",
                "status", "saved",
                "email", "xxxx@dome.tu.ac.th",
                "phone", "0812345678",
                "name", "นางสาว XXX",
                "requestDate", "16/08/2569",
                "type", "ห้องปฏิบัติการคอมพิวเตอร์",
                "steps", List.of("saved", "inspected", "reviewed")
            ),
            Map.of(
                "id", "128397460",
                "date", "25/03/2569",
                "room", "LC2-209",
                "time", "15:00-18:00",
                "status", "rejected",
                "email", "yyy@dome.tu.ac.th",
                "phone", "0899999999",
                "name", "นาย YYY",
                "requestDate", "30/03/2569",
                "type", "ห้องเรียน",
                "steps", List.of("saved", "inspected", "reviewed", "rejected")
            )
        );
    }
}
