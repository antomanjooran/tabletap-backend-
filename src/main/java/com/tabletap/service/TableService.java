package com.tabletap.service;

import com.tabletap.dto.response.TableResponse;
import com.tabletap.exception.ApiException;
import com.tabletap.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;

    @Transactional(readOnly = true)
    public TableResponse validateQrToken(String token) {
        return tableRepository.findByQrTokenAndIsActiveTrue(token)
                .map(t -> new TableResponse(t.getId(), t.getNumber(), t.getName(), t.getCapacity()))
                .orElseThrow(() -> new ApiException("Invalid or inactive QR code", "INVALID_QR", 404));
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getAllTables() {
        return tableRepository.findAllByIsActiveTrueOrderByNumber().stream()
                .map(t -> new TableResponse(t.getId(), t.getNumber(), t.getName(), t.getCapacity()))
                .toList();
    }
}
