package com.tabletap.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "menu_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItem {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false) private Category category;
    @Column(nullable = false) private String name;
    private String description;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    private String emoji;
    @Column(name = "image_url") private String imageUrl;
    @Builder.Default @Column(name = "is_available", nullable = false) private Boolean isAvailable = true;
    @Column(name = "quantity_available") private Integer quantityAvailable;
    @Builder.Default @Column(name = "sort_order") private Integer sortOrder = 0;
    @Column(name = "created_at", updatable = false) @Builder.Default private Instant createdAt = Instant.now();
    @Column(name = "updated_at") @Builder.Default private Instant updatedAt = Instant.now();
    @PreUpdate void onUpdate() { this.updatedAt = Instant.now(); }
}
