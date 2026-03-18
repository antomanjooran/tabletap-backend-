package com.tabletap.repository;

import com.tabletap.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("""
        SELECT c FROM Category c
        LEFT JOIN FETCH c.menuItems m
        WHERE c.isActive = true AND m.isAvailable = true
        ORDER BY c.sortOrder ASC, m.sortOrder ASC
    """)
    List<Category> findActiveWithAvailableItems();
}
