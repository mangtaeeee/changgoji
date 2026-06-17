package com.warehouse.inbound.repository;

import static com.warehouse.inbound.domain.QInboundItem.inboundItem;
import static com.warehouse.inbound.domain.QInboundOrder.inboundOrder;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.warehouse.inbound.domain.InboundOrder;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InboundOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<InboundOrder> findByIdWithItems(Long id) {
        return Optional.ofNullable(queryFactory
            .selectFrom(inboundOrder)
            .distinct()
            .leftJoin(inboundOrder.items, inboundItem).fetchJoin()
            .where(inboundOrder.id.eq(id))
            .fetchOne());
    }
}
