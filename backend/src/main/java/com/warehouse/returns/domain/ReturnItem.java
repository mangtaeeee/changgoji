package com.warehouse.returns.domain;

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
@Table(name = "return_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_order_id", nullable = false)
    private ReturnOrder returnOrder;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private String skuName;

    @Column(nullable = false)
    private int requestedQty;

    @Column(nullable = false)
    private int receivedQty;

    @Enumerated(EnumType.STRING)
    private ItemCondition condition;

    public static ReturnItem create(ReturnOrder returnOrder, Long skuId, String skuName, int requestedQty) {
        ReturnItem item = new ReturnItem();
        item.returnOrder = returnOrder;
        item.skuId = skuId;
        item.skuName = skuName;
        item.requestedQty = requestedQty;
        item.receivedQty = 0;
        return item;
    }

    public void receive(int receivedQty, ItemCondition condition) {
        this.receivedQty = receivedQty;
        this.condition = condition;
    }
}
