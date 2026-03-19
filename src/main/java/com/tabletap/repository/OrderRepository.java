=package com.tabletap.repository;
import com.tabletap.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.*;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o JOIN FETCH o.table t LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem m WHERE o.status NOT IN ('SERVED','CANCELLED') ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersWithDetails();

    @Query("SELECT o FROM Order o JOIN FETCH o.table t LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem m WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT o FROM Order o JOIN FETCH o.table t LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem m WHERE o.createdAt >= :start AND o.createdAt < :end ORDER BY o.createdAt DESC")
    List<Order> findByDateRangeWithDetails(@Param("start") Instant start, @Param("end") Instant end);
}