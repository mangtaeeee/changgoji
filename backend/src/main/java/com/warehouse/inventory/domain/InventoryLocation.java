package com.warehouse.inventory.domain;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "inventory_location")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(nullable = false)
    private String locationCode;

    @Column(nullable = false)
    private int qty;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static InventoryLocation create(Inventory inventory, String locationCode, int qty) {
        InventoryLocation location = new InventoryLocation();
        location.inventory = inventory;
        location.locationCode = locationCode;
        location.qty = qty;
        return location;
    }

    public void increase(int qty) {
        this.qty += qty;
    }

    public void decrease(int qty) {
        if (this.qty < qty) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.qty -= qty;
    }
}
