package com.warehouse.inbound.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInboundOrder is a Querydsl query type for InboundOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInboundOrder extends EntityPathBase<InboundOrder> {

    private static final long serialVersionUID = 1603046162L;

    public static final QInboundOrder inboundOrder = new QInboundOrder("inboundOrder");

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<InboundItem, QInboundItem> items = this.<InboundItem, QInboundItem>createList("items", InboundItem.class, QInboundItem.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> scheduledDate = createDate("scheduledDate", java.time.LocalDate.class);

    public final EnumPath<InboundStatus> status = createEnum("status", InboundStatus.class);

    public final NumberPath<Long> supplierId = createNumber("supplierId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> warehouseId = createNumber("warehouseId", Long.class);

    public QInboundOrder(String variable) {
        super(InboundOrder.class, forVariable(variable));
    }

    public QInboundOrder(Path<? extends InboundOrder> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInboundOrder(PathMetadata metadata) {
        super(InboundOrder.class, metadata);
    }

}

