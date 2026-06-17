package com.warehouse.picking.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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

@Getter
@Entity
@Table(name = "picking_wave", indexes = @Index(name = "idx_picking_wave_warehouse_status", columnList = "warehouse_id, status"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickingWave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long outboundOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickingWaveStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "wave", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PickingTask> tasks = new ArrayList<>();

    public static PickingWave create(Long warehouseId, Long outboundOrderId) {
        PickingWave wave = new PickingWave();
        wave.warehouseId = warehouseId;
        wave.outboundOrderId = outboundOrderId;
        wave.status = PickingWaveStatus.OPEN;
        return wave;
    }

    public void addTask(Long outboundItemId, Long skuId, String locationCode, int qty) {
        tasks.add(PickingTask.create(this, outboundItemId, skuId, locationCode, qty));
    }

    public void start() {
        this.status = PickingWaveStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = PickingWaveStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
