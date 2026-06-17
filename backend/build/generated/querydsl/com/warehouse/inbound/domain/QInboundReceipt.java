package com.warehouse.inbound.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInboundReceipt is a Querydsl query type for InboundReceipt
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInboundReceipt extends EntityPathBase<InboundReceipt> {

    private static final long serialVersionUID = 923505916L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInboundReceipt inboundReceipt = new QInboundReceipt("inboundReceipt");

    public final DateTimePath<java.time.LocalDateTime> confirmedAt = createDateTime("confirmedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> confirmedBy = createNumber("confirmedBy", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInboundOrder inboundOrder;

    public final StringPath memo = createString("memo");

    public QInboundReceipt(String variable) {
        this(InboundReceipt.class, forVariable(variable), INITS);
    }

    public QInboundReceipt(Path<? extends InboundReceipt> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInboundReceipt(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInboundReceipt(PathMetadata metadata, PathInits inits) {
        this(InboundReceipt.class, metadata, inits);
    }

    public QInboundReceipt(Class<? extends InboundReceipt> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.inboundOrder = inits.isInitialized("inboundOrder") ? new QInboundOrder(forProperty("inboundOrder")) : null;
    }

}

