package com.warehouse.outbound.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOutboundItem is a Querydsl query type for OutboundItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOutboundItem extends EntityPathBase<OutboundItem> {

    private static final long serialVersionUID = 1581514631L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOutboundItem outboundItem = new QOutboundItem("outboundItem");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath locationCode = createString("locationCode");

    public final QOutboundOrder outboundOrder;

    public final NumberPath<Integer> requestedQty = createNumber("requestedQty", Integer.class);

    public final NumberPath<Integer> shippedQty = createNumber("shippedQty", Integer.class);

    public final NumberPath<Long> skuId = createNumber("skuId", Long.class);

    public QOutboundItem(String variable) {
        this(OutboundItem.class, forVariable(variable), INITS);
    }

    public QOutboundItem(Path<? extends OutboundItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOutboundItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOutboundItem(PathMetadata metadata, PathInits inits) {
        this(OutboundItem.class, metadata, inits);
    }

    public QOutboundItem(Class<? extends OutboundItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.outboundOrder = inits.isInitialized("outboundOrder") ? new QOutboundOrder(forProperty("outboundOrder")) : null;
    }

}

