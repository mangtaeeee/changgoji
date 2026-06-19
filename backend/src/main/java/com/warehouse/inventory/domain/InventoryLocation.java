package com.warehouse.inventory.domain;

import com.warehouse.common.exception.InvalidInputException;
import com.warehouse.inventory.exception.InsufficientStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(
    name = "inventory_location",
    uniqueConstraints = @UniqueConstraint(name = "uk_inventory_location_inventory_code", columnNames = {"inventory_id", "location_code"})
)
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
        validatePositiveQty(qty);
        this.qty += qty;
    }

    public void decrease(int qty) {
        validatePositiveQty(qty);
        if (this.qty < qty) {
            throw new InsufficientStockException();
        }
        this.qty -= qty;
    }

    private void validatePositiveQty(int qty) {
        if (qty <= 0) {
            throw new InvalidInputException("수량은 1 이상이어야 합니다.");
        }
    }
}
