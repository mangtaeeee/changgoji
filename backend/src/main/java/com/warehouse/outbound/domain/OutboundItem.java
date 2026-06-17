package com.warehouse.outbound.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "outbound_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outbound_order_id", nullable = false)
    private OutboundOrder outboundOrder;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private int requestedQty;

    @Column(nullable = false)
    private int shippedQty;

    @Column(nullable = false)
    private String locationCode;

    public static OutboundItem create(OutboundOrder outboundOrder, Long skuId, int requestedQty, String locationCode) {
        OutboundItem item = new OutboundItem();
        item.outboundOrder = outboundOrder;
        item.skuId = skuId;
        item.requestedQty = requestedQty;
        item.shippedQty = 0;
        item.locationCode = locationCode;
        return item;
    }

    public void ship() {
        this.shippedQty = requestedQty;
    }
}
