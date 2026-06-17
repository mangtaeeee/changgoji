package com.warehouse.returns.repository;

import com.warehouse.returns.domain.ReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Long> {
}
