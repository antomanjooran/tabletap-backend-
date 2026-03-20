package com.tabletap.controller;

import com.tabletap.dto.request.CategoryRequest;
import com.tabletap.dto.request.ReorderRequest;
import com.tabletap.dto.response.CategorySimpleResponse;
import com.tabletap.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategorySimpleResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @PostMapping
    public ResponseEntity<CategorySimpleResponse> createCategory(@RequestBody CategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategorySimpleResponse> updateCategory(
            @PathVariable UUID id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(categoryService.updateCategory(id, updates));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody List<ReorderRequest> updates) {
        categoryService.reorder(updates);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}