package com.warehouse.outbound.repository;

import com.warehouse.outbound.domain.OutboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long> {
}
