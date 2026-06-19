package com.warehouse.shipping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.common.exception.InvalidInputException;
import com.warehouse.outbound.exception.OutboundOrderNotFoundException;
import com.warehouse.outbound.repository.OutboundOrderRepository;
import com.warehouse.shipping.domain.ShippingLabel;
import com.warehouse.shipping.exception.ShippingLabelNotFoundException;
import com.warehouse.shipping.repository.ShippingLabelRepository;
import com.warehouse.shipping.service.dto.ShippingLabelCreateRequest;
import com.warehouse.shipping.service.dto.ShippingLabelFailRequest;
import com.warehouse.shipping.service.dto.ShippingLabelResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShippingLabelService {

    private final ShippingLabelRepository shippingLabelRepository;
    private final OutboundOrderRepository outboundOrderRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ShippingLabelResponse createShippingLabel(ShippingLabelCreateRequest request) {
        outboundOrderRepository.findById(request.outboundOrderId())
            .orElseThrow(OutboundOrderNotFoundException::new);

        String trackingNo = generateTrackingNo(request.outboundOrderId());
        ShippingLabel label = ShippingLabel.create(
            request.outboundOrderId(),
            trackingNo,
            request.carrier(),
            request.receiverName(),
            request.receiverPhone(),
            request.receiverAddress(),
            createLabelData(request, trackingNo)
        );
        return ShippingLabelResponse.from(shippingLabelRepository.save(label));
    }

    public ShippingLabelResponse getShippingLabel(Long id) {
        return ShippingLabelResponse.from(getLabel(id));
    }

    public ShippingLabelResponse getShippingLabelByOutboundOrder(Long outboundOrderId) {
        return ShippingLabelResponse.from(shippingLabelRepository.findByOutboundOrderId(outboundOrderId)
            .orElseThrow(ShippingLabelNotFoundException::new));
    }

    @Transactional
    public ShippingLabelResponse requestPrint(Long id) {
        ShippingLabel label = getLabel(id);
        label.requestPrint();
        return ShippingLabelResponse.from(label);
    }

    @Transactional
    public ShippingLabelResponse markPrinted(Long id) {
        ShippingLabel label = getLabel(id);
        label.markPrinted();
        return ShippingLabelResponse.from(label);
    }

    @Transactional
    public ShippingLabelResponse markFailed(Long id, ShippingLabelFailRequest request) {
        ShippingLabel label = getLabel(id);
        label.markFailed(request.failureReason());
        return ShippingLabelResponse.from(label);
    }

    private ShippingLabel getLabel(Long id) {
        return shippingLabelRepository.findById(id)
            .orElseThrow(ShippingLabelNotFoundException::new);
    }

    private String generateTrackingNo(Long outboundOrderId) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "CJ%s%06d".formatted(date, outboundOrderId);
    }

    private String createLabelData(ShippingLabelCreateRequest request, String trackingNo) {
        try {
            return objectMapper.writeValueAsString(new ShippingLabelPayload(
                trackingNo,
                request.carrier(),
                request.receiverName(),
                request.receiverPhone(),
                request.receiverAddress(),
                request.outboundOrderId()
            ));
        } catch (JsonProcessingException e) {
            throw new InvalidInputException("송장 데이터를 생성할 수 없습니다.");
        }
    }

    private record ShippingLabelPayload(
        String trackingNo,
        String carrier,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        Long outboundOrderId
    ) {
    }
}
