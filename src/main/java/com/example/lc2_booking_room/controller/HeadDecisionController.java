package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.headDecision.HeadDecisionRequest;
import com.example.lc2_booking_room.dto.headDecision.HeadDecisionView;
import com.example.lc2_booking_room.service.headDecision.HeadDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/head-decide")   // ✅ path สื่อชัดเจน
@RequiredArgsConstructor
public class HeadDecisionController {

    private final HeadDecisionService service;

    @GetMapping("/{id}")
    public ResponseEntity<HeadDecisionView> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReservationForHead(id));
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<Map<String, Object>> decide(
            @PathVariable Long id,
            @RequestBody HeadDecisionRequest req
    ) {
        service.decide(id, req);

        Map<String, Object> response = Map.of(
                "reservationId", id,
                "status", "SUCCESS",
                "finalDecision", req.getDecision(),
                "remark", req.getRemark()
        );

        return ResponseEntity.ok(response);
    }
}
