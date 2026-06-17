package com.warehouse.inbound.domain;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "inbound_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inbound_order_id", nullable = false)
    private InboundOrder inboundOrder;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private String skuName;

    @Column(nullable = false)
    private int orderedQty;

    @Column(nullable = false)
    private int receivedQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboundItemStatus status;

    public static InboundItem create(InboundOrder inboundOrder, Long skuId, String skuName, int orderedQty) {
        InboundItem item = new InboundItem();
        item.inboundOrder = inboundOrder;
        item.skuId = skuId;
        item.skuName = skuName;
        item.orderedQty = orderedQty;
        item.receivedQty = 0;
        item.status = InboundItemStatus.PENDING;
        return item;
    }

    public void receive(int receivedQty) {
        this.receivedQty = receivedQty;
        this.status = receivedQty >= orderedQty ? InboundItemStatus.COMPLETED : InboundItemStatus.PARTIAL;
    }
}
