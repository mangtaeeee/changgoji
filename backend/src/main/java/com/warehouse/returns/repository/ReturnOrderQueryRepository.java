package com.warehouse.returns.repository;

import static com.warehouse.returns.domain.QReturnItem.returnItem;
import static com.warehouse.returns.domain.QReturnOrder.returnOrder;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.warehouse.returns.domain.ReturnOrder;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReturnOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<ReturnOrder> findByIdWithItems(Long id) {
        return Optional.ofNullable(queryFactory
            .selectFrom(returnOrder)
            .distinct()
            .leftJoin(returnOrder.items, returnItem).fetchJoin()
            .where(returnOrder.id.eq(id))
            .fetchOne());
    }
}
