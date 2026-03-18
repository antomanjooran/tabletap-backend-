package com.tabletap.service;

import com.tabletap.dto.response.TableInfo;
import com.tabletap.dto.response.WaiterCallResponse;
import com.tabletap.entity.RestaurantTable;
import com.tabletap.entity.WaiterCall;
import com.tabletap.exception.ApiException;
import com.tabletap.repository.TableRepository;
import com.tabletap.repository.WaiterCallRepository;
import com.tabletap.websocket.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaiterService {

    private final WaiterCallRepository waiterCallRepository;
    private final TableRepository      tableRepository;
    private final RealtimeService      realtimeService;

    @Transactional
    public WaiterCallResponse callWaiter(UUID tableId, String message) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ApiException("Table not found", "TABLE_NOT_FOUND", 404));

        WaiterCall call = WaiterCall.builder().table(table)
                .message(message != null ? message : "Waiter requested").build();

        WaiterCall saved = waiterCallRepository.save(call);
        log.info("Waiter called at table {}", table.getNumber());

        WaiterCallResponse response = toResponse(saved);
        realtimeService.broadcastWaiterCall(response);
        return response;
    }

    @Transactional
    public void resolveCall(UUID callId) {
        WaiterCall call = waiterCallRepository.findById(callId)
                .orElseThrow(() -> new ApiException("Waiter call not found", "CALL_NOT_FOUND", 404));
        call.setIsResolved(true);
        call.setResolvedAt(Instant.now());
        waiterCallRepository.save(call);
    }

    @Transactional(readOnly = true)
    public List<WaiterCallResponse> getActiveCalls() {
        return waiterCallRepository.findUnresolvedWithTable().stream().map(this::toResponse).toList();
    }

    private WaiterCallResponse toResponse(WaiterCall c) {
        return new WaiterCallResponse(c.getId(), c.getMessage(),
            new TableInfo(c.getTable().getId(), c.getTable().getNumber(), c.getTable().getName()),
            c.getCreatedAt());
    }
}
