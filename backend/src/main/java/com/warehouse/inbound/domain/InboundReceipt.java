package com.warehouse.inbound.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "inbound_receipt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboundReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inbound_order_id", nullable = false, unique = true)
    private InboundOrder inboundOrder;

    @Column(nullable = false)
    private Long confirmedBy;

    @Column(nullable = false)
    private LocalDateTime confirmedAt;

    @Column(columnDefinition = "text")
    private String memo;

    public static InboundReceipt create(InboundOrder inboundOrder, Long confirmedBy, String memo) {
        InboundReceipt receipt = new InboundReceipt();
        receipt.inboundOrder = inboundOrder;
        receipt.confirmedBy = confirmedBy;
        receipt.confirmedAt = LocalDateTime.now();
        receipt.memo = memo;
        return receipt;
    }
}
