package com.warehouse.returns.domain;

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
@Table(name = "return_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long outboundOrderId;

    @Column(nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnReason reason;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "returnOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnItem> items = new ArrayList<>();

    public static ReturnOrder create(Long outboundOrderId, Long warehouseId, ReturnReason reason) {
        ReturnOrder order = new ReturnOrder();
        order.outboundOrderId = outboundOrderId;
        order.warehouseId = warehouseId;
        order.reason = reason;
        order.status = ReturnStatus.REQUESTED;
        order.requestedAt = LocalDateTime.now();
        return order;
    }

    public void addItem(Long skuId, String skuName, int requestedQty) {
        items.add(ReturnItem.create(this, skuId, skuName, requestedQty));
    }

    public void receive() {
        this.status = ReturnStatus.RECEIVED;
    }

    public void complete() {
        this.status = ReturnStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = ReturnStatus.REJECTED;
    }
}
