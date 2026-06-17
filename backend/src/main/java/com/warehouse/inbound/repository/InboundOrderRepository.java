package com.warehouse.inbound.repository;

import com.warehouse.inbound.domain.InboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {
}
