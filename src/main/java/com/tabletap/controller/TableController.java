package com.tabletap.controller;

import com.tabletap.dto.response.TableResponse;
import com.tabletap.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping("/validate")
    public ResponseEntity<TableResponse> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(tableService.validateQrToken(token));
    }

    @GetMapping
    public ResponseEntity<List<TableResponse>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }
}
