package com.tabletap.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantTable {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true)
    private Integer number;

    private String name;

    @Builder.Default
    @Column(nullable = false)
    private Integer capacity = 4;

    @Column(name = "qr_token", nullable = false, unique = true)
    private String qrToken;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}