package com.warehouse.inbound.repository;

import com.warehouse.inbound.domain.InboundReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundReceiptRepository extends JpaRepository<InboundReceipt, Long> {
}
