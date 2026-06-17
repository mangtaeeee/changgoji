package com.warehouse.outbound.repository;

import static com.warehouse.outbound.domain.QOutboundItem.outboundItem;
import static com.warehouse.outbound.domain.QOutboundOrder.outboundOrder;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.warehouse.outbound.domain.OutboundOrder;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboundOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<OutboundOrder> findByIdWithItems(Long id) {
        return Optional.ofNullable(queryFactory
            .selectFrom(outboundOrder)
            .distinct()
            .leftJoin(outboundOrder.items, outboundItem).fetchJoin()
            .where(outboundOrder.id.eq(id))
            .fetchOne());
    }
}
