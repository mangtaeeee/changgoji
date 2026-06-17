package com.warehouse.inventory.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInventoryLocation is a Querydsl query type for InventoryLocation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryLocation extends EntityPathBase<InventoryLocation> {

    private static final long serialVersionUID = -440294127L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInventoryLocation inventoryLocation = new QInventoryLocation("inventoryLocation");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInventory inventory;

    public final StringPath locationCode = createString("locationCode");

    public final NumberPath<Integer> qty = createNumber("qty", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QInventoryLocation(String variable) {
        this(InventoryLocation.class, forVariable(variable), INITS);
    }

    public QInventoryLocation(Path<? extends InventoryLocation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInventoryLocation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInventoryLocation(PathMetadata metadata, PathInits inits) {
        this(InventoryLocation.class, metadata, inits);
    }

    public QInventoryLocation(Class<? extends InventoryLocation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.inventory = inits.isInitialized("inventory") ? new QInventory(forProperty("inventory")) : null;
    }

}

