package com.tabletap.controller;

import com.tabletap.dto.request.UpdateAvailabilityRequest;
import com.tabletap.dto.response.MenuItemResponse;
import com.tabletap.dto.response.MenuResponse;
import com.tabletap.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuResponse>> getMenu() {
        return ResponseEntity.ok(menuService.getMenu());
    }

    @PatchMapping("/items/{id}/availability")
    public ResponseEntity<Void> setAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        menuService.setAvailability(id, request.isAvailable());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> updateItem(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(menuService.updateItem(id, updates));
    }
}
