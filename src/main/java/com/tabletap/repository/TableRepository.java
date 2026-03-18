package com.tabletap.repository;

import com.tabletap.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, UUID> {
    Optional<RestaurantTable> findByQrTokenAndIsActiveTrue(String qrToken);
    List<RestaurantTable> findAllByIsActiveTrueOrderByNumber();
}
