package com.warehouse.shipping.repository;

import com.warehouse.shipping.domain.ShippingLabel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingLabelRepository extends JpaRepository<ShippingLabel, Long> {

    Optional<ShippingLabel> findByOutboundOrderId(Long outboundOrderId);
}
