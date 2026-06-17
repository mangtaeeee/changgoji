package com.warehouse.outbound.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOutboundOrder is a Querydsl query type for OutboundOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOutboundOrder extends EntityPathBase<OutboundOrder> {

    private static final long serialVersionUID = 1787793754L;

    public static final QOutboundOrder outboundOrder = new QOutboundOrder("outboundOrder");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<OutboundItem, QOutboundItem> items = this.<OutboundItem, QOutboundItem>createList("items", OutboundItem.class, QOutboundItem.class, PathInits.DIRECT2);

    public final StringPath orderId = createString("orderId");

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> shippedAt = createDateTime("shippedAt", java.time.LocalDateTime.class);

    public final EnumPath<OutboundStatus> status = createEnum("status", OutboundStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> warehouseId = createNumber("warehouseId", Long.class);

    public QOutboundOrder(String variable) {
        super(OutboundOrder.class, forVariable(variable));
    }

    public QOutboundOrder(Path<? extends OutboundOrder> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOutboundOrder(PathMetadata metadata) {
        super(OutboundOrder.class, metadata);
    }

}

