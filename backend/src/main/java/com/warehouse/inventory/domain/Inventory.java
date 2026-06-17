package com.warehouse.inventory.domain;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "inventory", indexes = @Index(name = "idx_inventory_warehouse_sku", columnList = "warehouse_id, sku_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private String skuName;

    @Column(nullable = false)
    private int availableQty;

    @Column(nullable = false)
    private int allocatedQty;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static Inventory create(Long warehouseId, Long skuId, String skuName, int availableQty) {
        Inventory inventory = new Inventory();
        inventory.warehouseId = warehouseId;
        inventory.skuId = skuId;
        inventory.skuName = skuName;
        inventory.availableQty = availableQty;
        inventory.allocatedQty = 0;
        return inventory;
    }

    public void allocate(int qty) {
        if (availableQty < qty) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.availableQty -= qty;
        this.allocatedQty += qty;
    }

    public void release(int qty) {
        this.allocatedQty -= qty;
        this.availableQty += qty;
    }

    public void ship(int qty) {
        this.allocatedQty -= qty;
    }

    public void adjust(int adjustQty) {
        this.availableQty += adjustQty;
    }
}
