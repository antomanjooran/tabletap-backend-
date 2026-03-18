package com.tabletap.controller;

import com.tabletap.dto.request.WaiterCallRequest;
import com.tabletap.dto.response.WaiterCallResponse;
import com.tabletap.service.WaiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/waiter")
@RequiredArgsConstructor
public class WaiterController {

    private final WaiterService waiterService;

    @PostMapping("/calls")
    public ResponseEntity<WaiterCallResponse> callWaiter(@Valid @RequestBody WaiterCallRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(waiterService.callWaiter(request.tableId(), request.message()));
    }

    @GetMapping("/calls/active")
    public ResponseEntity<List<WaiterCallResponse>> getActiveCalls() {
        return ResponseEntity.ok(waiterService.getActiveCalls());
    }

    @DeleteMapping("/calls/{id}")
    public ResponseEntity<Void> resolveCall(@PathVariable UUID id) {
        waiterService.resolveCall(id);
        return ResponseEntity.noContent().build();
    }
}
