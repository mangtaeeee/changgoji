package com.warehouse.inventory.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInventoryHistory is a Querydsl query type for InventoryHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryHistory extends EntityPathBase<InventoryHistory> {

    private static final long serialVersionUID = 1544143800L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInventoryHistory inventoryHistory = new QInventoryHistory("inventoryHistory");

    public final NumberPath<Integer> afterQty = createNumber("afterQty", Integer.class);

    public final NumberPath<Integer> beforeQty = createNumber("beforeQty", Integer.class);

    public final NumberPath<Integer> changeQty = createNumber("changeQty", Integer.class);

    public final EnumPath<ChangeType> changeType = createEnum("changeType", ChangeType.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInventory inventory;

    public final NumberPath<Long> referenceId = createNumber("referenceId", Long.class);

    public QInventoryHistory(String variable) {
        this(InventoryHistory.class, forVariable(variable), INITS);
    }

    public QInventoryHistory(Path<? extends InventoryHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInventoryHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInventoryHistory(PathMetadata metadata, PathInits inits) {
        this(InventoryHistory.class, metadata, inits);
    }

    public QInventoryHistory(Class<? extends InventoryHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.inventory = inits.isInitialized("inventory") ? new QInventory(forProperty("inventory")) : null;
    }

}

