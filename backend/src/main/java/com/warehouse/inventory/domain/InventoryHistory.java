package com.warehouse.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "inventory_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeType changeType;

    @Column(nullable = false)
    private int beforeQty;

    @Column(nullable = false)
    private int afterQty;

    @Column(nullable = false)
    private int changeQty;

    private Long referenceId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static InventoryHistory create(Inventory inventory, ChangeType changeType, int beforeQty, int afterQty,
        int changeQty, Long referenceId) {
        InventoryHistory history = new InventoryHistory();
        history.inventory = inventory;
        history.changeType = changeType;
        history.beforeQty = beforeQty;
        history.afterQty = afterQty;
        history.changeQty = changeQty;
        history.referenceId = referenceId;
        return history;
    }
}
