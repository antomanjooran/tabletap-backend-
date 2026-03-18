package com.tabletap.repository;

import com.tabletap.entity.WaiterCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WaiterCallRepository extends JpaRepository<WaiterCall, UUID> {

    @Query("""
        SELECT w FROM WaiterCall w
        JOIN FETCH w.table t
        WHERE w.isResolved = false
        ORDER BY w.createdAt ASC
    """)
    List<WaiterCall> findUnresolvedWithTable();
}
