package com.warehouse.inbound.domain;

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
import java.time.LocalDate;
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
@Table(name = "inbound_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboundStatus status;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "inboundOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboundItem> items = new ArrayList<>();

    public static InboundOrder create(Long warehouseId, Long supplierId, LocalDate scheduledDate) {
        InboundOrder order = new InboundOrder();
        order.warehouseId = warehouseId;
        order.supplierId = supplierId;
        order.scheduledDate = scheduledDate;
        order.status = InboundStatus.REQUESTED;
        return order;
    }

    public void addItem(Long skuId, String skuName, int orderedQty) {
        items.add(InboundItem.create(this, skuId, skuName, orderedQty));
    }

    public void startReceiving() {
        this.status = InboundStatus.RECEIVING;
    }

    public void confirm() {
        this.status = InboundStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}

