package com.warehouse.inbound.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInboundItem is a Querydsl query type for InboundItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInboundItem extends EntityPathBase<InboundItem> {

    private static final long serialVersionUID = 1298460367L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInboundItem inboundItem = new QInboundItem("inboundItem");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInboundOrder inboundOrder;

    public final NumberPath<Integer> orderedQty = createNumber("orderedQty", Integer.class);

    public final NumberPath<Integer> receivedQty = createNumber("receivedQty", Integer.class);

    public final NumberPath<Long> skuId = createNumber("skuId", Long.class);

    public final StringPath skuName = createString("skuName");

    public final EnumPath<InboundItemStatus> status = createEnum("status", InboundItemStatus.class);

    public QInboundItem(String variable) {
        this(InboundItem.class, forVariable(variable), INITS);
    }

    public QInboundItem(Path<? extends InboundItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInboundItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInboundItem(PathMetadata metadata, PathInits inits) {
        this(InboundItem.class, metadata, inits);
    }

    public QInboundItem(Class<? extends InboundItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.inboundOrder = inits.isInitialized("inboundOrder") ? new QInboundOrder(forProperty("inboundOrder")) : null;
    }

}

