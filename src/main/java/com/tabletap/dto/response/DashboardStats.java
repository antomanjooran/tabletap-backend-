package com.tabletap.dto.response;

import java.math.BigDecimal;

public record DashboardStats(
        long totalOrders,
        long completedOrders,
        long pendingOrders,
        long preparingOrders,
        long readyOrders,
        BigDecimal revenue
) {}
