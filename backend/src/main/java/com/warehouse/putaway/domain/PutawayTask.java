package com.warehouse.putaway.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "putaway_task", indexes = @Index(name = "idx_putaway_warehouse_status", columnList = "warehouse_id, status"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PutawayTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inboundOrderId;

    @Column(nullable = false)
    private Long inboundItemId;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private String skuName;

    @Column(nullable = false)
    private int qty;

    @Column(nullable = false)
    private String recommendedLocation;

    private String confirmedLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PutawayStatus status;

    private Long assignedTo;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public static PutawayTask create(Long inboundOrderId, Long inboundItemId, Long warehouseId, Long skuId,
        String skuName, int qty, String recommendedLocation) {
        PutawayTask task = new PutawayTask();
        task.inboundOrderId = inboundOrderId;
        task.inboundItemId = inboundItemId;
        task.warehouseId = warehouseId;
        task.skuId = skuId;
        task.skuName = skuName;
        task.qty = qty;
        task.recommendedLocation = recommendedLocation;
        task.status = PutawayStatus.PENDING;
        return task;
    }

    public void start(Long assignedTo) {
        this.assignedTo = assignedTo;
        this.status = PutawayStatus.IN_PROGRESS;
    }

    public void confirm(String confirmedLocation) {
        this.confirmedLocation = confirmedLocation;
        this.status = PutawayStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
