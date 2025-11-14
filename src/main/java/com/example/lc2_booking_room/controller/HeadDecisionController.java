package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.headDecision.HeadDecisionRequest;
import com.example.lc2_booking_room.dto.headDecision.HeadDecisionView;
import com.example.lc2_booking_room.service.headDecision.HeadDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> decide(
            @PathVariable Long id,
            @RequestBody HeadDecisionRequest req
    ) {
        service.decide(id, req);
        return ResponseEntity.ok().build();
    }
}
