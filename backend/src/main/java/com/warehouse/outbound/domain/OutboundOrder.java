package com.warehouse.outbound.domain;

import com.warehouse.common.exception.InvalidStatusException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "outbound_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboundStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime shippedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundItem> items = new ArrayList<>();

    public static OutboundOrder create(Long warehouseId, String orderId) {
        OutboundOrder order = new OutboundOrder();
        order.warehouseId = warehouseId;
        order.orderId = orderId;
        order.status = OutboundStatus.PENDING;
        order.requestedAt = LocalDateTime.now();
        return order;
    }

    public void addItem(Long skuId, int requestedQty, String locationCode) {
        items.add(OutboundItem.create(this, skuId, requestedQty, locationCode));
    }

    public void allocate() {
        if (status != OutboundStatus.PENDING) {
            throw new InvalidStatusException();
        }
        this.status = OutboundStatus.ALLOCATED;
    }

    public void ship() {
        if (status != OutboundStatus.ALLOCATED) {
            throw new InvalidStatusException();
        }
        this.status = OutboundStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == OutboundStatus.SHIPPED || status == OutboundStatus.CANCELLED) {
            throw new InvalidStatusException();
        }
        this.status = OutboundStatus.CANCELLED;
    }
}

