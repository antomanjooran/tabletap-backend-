package com.tabletap.repository;

import com.tabletap.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.table t
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.menuItem m
        WHERE o.status NOT IN ('SERVED', 'CANCELLED')
        ORDER BY o.createdAt ASC
    """)
    List<Order> findActiveOrdersWithDetails();

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.table t
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.menuItem m
        WHERE o.id = :id
    """)
    Optional<Order> findByIdWithDetails(UUID id);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :start AND o.createdAt <= :end")
    List<Order> findByCreatedAtBetween(Instant start, Instant end);
}
