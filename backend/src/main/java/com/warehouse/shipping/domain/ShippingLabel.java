package com.warehouse.shipping.domain;

import com.warehouse.common.exception.InvalidStatusException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "shipping_label", indexes = @Index(name = "idx_shipping_label_outbound_order", columnList = "outbound_order_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long outboundOrderId;

    @Column(nullable = false, unique = true)
    private String trackingNo;

    @Column(nullable = false)
    private String carrier;

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String receiverPhone;

    @Column(nullable = false)
    private String receiverAddress;

    @Column(columnDefinition = "text", nullable = false)
    private String labelData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShippingLabelStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime printRequestedAt;

    private LocalDateTime printedAt;

    private String failureReason;

    public static ShippingLabel create(Long outboundOrderId, String trackingNo, String carrier, String receiverName,
        String receiverPhone, String receiverAddress, String labelData) {
        ShippingLabel label = new ShippingLabel();
        label.outboundOrderId = outboundOrderId;
        label.trackingNo = trackingNo;
        label.carrier = carrier;
        label.receiverName = receiverName;
        label.receiverPhone = receiverPhone;
        label.receiverAddress = receiverAddress;
        label.labelData = labelData;
        label.status = ShippingLabelStatus.PENDING;
        return label;
    }

    public void requestPrint() {
        if (status != ShippingLabelStatus.PENDING && status != ShippingLabelStatus.FAILED) {
            throw new InvalidStatusException();
        }
        this.status = ShippingLabelStatus.PRINT_REQUESTED;
        this.printRequestedAt = LocalDateTime.now();
        this.failureReason = null;
    }

    public void markPrinted() {
        if (status != ShippingLabelStatus.PRINT_REQUESTED) {
            throw new InvalidStatusException();
        }
        this.status = ShippingLabelStatus.PRINTED;
        this.printedAt = LocalDateTime.now();
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        if (status != ShippingLabelStatus.PRINT_REQUESTED) {
            throw new InvalidStatusException();
        }
        this.status = ShippingLabelStatus.FAILED;
        this.failureReason = failureReason;
    }
}
