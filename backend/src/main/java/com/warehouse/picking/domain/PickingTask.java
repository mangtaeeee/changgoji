package com.warehouse.picking.domain;

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

@Getter
@Entity
@Table(name = "picking_task")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wave_id", nullable = false)
    private PickingWave wave;

    @Column(nullable = false)
    private Long outboundItemId;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private String locationCode;

    @Column(nullable = false)
    private int qty;

    private Long assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickingTaskStatus status;

    private LocalDateTime pickedAt;

    public static PickingTask create(PickingWave wave, Long outboundItemId, Long skuId, String locationCode, int qty) {
        PickingTask task = new PickingTask();
        task.wave = wave;
        task.outboundItemId = outboundItemId;
        task.skuId = skuId;
        task.locationCode = locationCode;
        task.qty = qty;
        task.status = PickingTaskStatus.PENDING;
        return task;
    }

    public void pick(Long assignedTo) {
        this.assignedTo = assignedTo;
        this.status = PickingTaskStatus.PICKED;
        this.pickedAt = LocalDateTime.now();
    }

    public void skip(Long assignedTo) {
        this.assignedTo = assignedTo;
        this.status = PickingTaskStatus.SKIPPED;
    }
}
