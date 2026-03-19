package com.tabletap.controller;

import com.tabletap.dto.request.UpdateAvailabilityRequest;
import com.tabletap.dto.response.*;
import com.tabletap.service.MenuService;
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

    /** Public — customer menu (available items only) */
    @GetMapping
    public ResponseEntity<List<MenuResponse>> getMenu() {
        return ResponseEntity.ok(menuService.getMenu());
    }

    /** Restaurant — all items including unavailable, for management */
    @GetMapping("/manage")
    public ResponseEntity<List<MenuResponse>> getMenuForManagement() {
        return ResponseEntity.ok(menuService.getAllMenuForManagement());
    }

    /** Restaurant — category list for dropdowns */
    @GetMapping("/categories")
    public ResponseEntity<List<CategorySimpleResponse>> getCategories() {
        return ResponseEntity.ok(menuService.getCategories());
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<Void> setAvailability(@PathVariable UUID id,
                                                @RequestBody UpdateAvailabilityRequest req) {
        menuService.setAvailability(id, req.isAvailable());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<MenuItemResponse> updateQuantity(@PathVariable UUID id,
                                                           @RequestBody Map<String, Object> body) {
        Object q = body.get("quantity");
        Integer quantity = q == null ? null : ((Number) q).intValue();
        return ResponseEntity.ok(menuService.updateQuantity(id, quantity));
    }

    /** Full item update — name, description, price, emoji, imageUrl, categoryId, quantityAvailable */
    @PatchMapping("/{id}")
    public ResponseEntity<MenuItemResponse> updateItem(@PathVariable UUID id,
                                                       @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(menuService.updateItem(id, updates));
    }
}