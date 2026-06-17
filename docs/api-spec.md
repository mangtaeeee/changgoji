# API 명세

> Base URL: `http://localhost:8080/api/v1`
> 전체 API는 Swagger UI에서 확인 가능: `http://localhost:8080/swagger-ui.html`

---

## 공통 응답 형식

### 성공
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": { }
}
```

### 실패
```json
{
  "success": false,
  "code": "INVENTORY_001",
  "message": "재고가 부족합니다.",
  "data": null
}
```

### ErrorCode 목록
| 코드 | 메시지 | HTTP 상태 |
|---|---|---|
| INVENTORY_001 | 재고가 부족합니다. | 400 |
| ORDER_001 | 유효하지 않은 상태 전이입니다. | 400 |
| COMMON_001 | 동시 요청으로 인해 처리에 실패했습니다. 다시 시도해주세요. | 409 |
| COMMON_002 | 입력값이 올바르지 않습니다. | 400 |
| INBOUND_001 | 입고 지시를 찾을 수 없습니다. | 404 |
| OUTBOUND_001 | 출고 지시를 찾을 수 없습니다. | 404 |
| RETURN_001 | 반품 지시를 찾을 수 없습니다. | 404 |

---

## 입고 API

### 입고 지시 등록
```
POST /inbound-orders
```
Request
```json
{
  "warehouseId": 1,
  "supplierId": 10,
  "scheduledDate": "2026-06-20",
  "items": [
    { "skuId": 100, "skuName": "상품A", "orderedQty": 50 },
    { "skuId": 101, "skuName": "상품B", "orderedQty": 30 }
  ]
}
```

### 입고 지시 목록 조회
```
GET /inbound-orders?warehouseId=1&status=REQUESTED&page=0&size=20
```

### 입고 지시 단건 조회
```
GET /inbound-orders/{id}
```

### 실사 입고 처리
```
PATCH /inbound-orders/{id}/receive
```
Request
```json
{
  "items": [
    { "inboundItemId": 1, "receivedQty": 48 },
    { "inboundItemId": 2, "receivedQty": 30 }
  ]
}
```

### 입고 확정 (→ 재고 반영)
```
PATCH /inbound-orders/{id}/confirm
```
Request
```json
{ "confirmedBy": 999, "memo": "일부 파손 1개 제외" }
```

---

## 재고 API

### 창고 전체 재고 조회
```
GET /inventories?warehouseId=1&page=0&size=20
```
Response data
```json
[
  {
    "id": 1,
    "skuId": 100,
    "skuName": "상품A",
    "availableQty": 48,
    "allocatedQty": 10,
    "locationCode": "A-01-03"
  }
]
```

### SKU별 재고 조회
```
GET /inventories/{skuId}?warehouseId=1
```

### 재고 조정 (실사)
```
POST /inventories/adjust
```
Request
```json
{
  "inventoryId": 1,
  "adjustQty": -2,
  "reason": "파손으로 인한 폐기"
}
```

### 재고 이력 조회
```
GET /inventories/{id}/history?page=0&size=20
```

---

## 출고 API

### 출고 지시 등록
```
POST /outbound-orders
```
Request
```json
{
  "warehouseId": 1,
  "orderId": "ORDER-20260617-001",
  "items": [
    { "skuId": 100, "requestedQty": 5, "locationCode": "A-01-03" }
  ]
}
```

### 출고 지시 목록 조회
```
GET /outbound-orders?warehouseId=1&status=PENDING&page=0&size=20
```

### 출고 지시 단건 조회
```
GET /outbound-orders/{id}
```

### 재고 할당
```
PATCH /outbound-orders/{id}/allocate
```
> 재고 차감(availableQty↓, allocatedQty↑) + 상태 PENDING → ALLOCATED
> 동시 요청 시 낙관적 락으로 처리, 최대 3회 재시도

### 출고 확정
```
PATCH /outbound-orders/{id}/ship
```
> allocatedQty 차감 확정 + 상태 PICKING → SHIPPED

### 출고 취소 (재고 보상 복구)
```
PATCH /outbound-orders/{id}/cancel
```
> ALLOCATED 상태였으면 allocatedQty → availableQty 복구 (보상 처리)

---

## 반품 API

### 반품 접수
```
POST /return-orders
```
Request
```json
{
  "outboundOrderId": 1,
  "warehouseId": 1,
  "reason": "CUSTOMER_CHANGE",
  "items": [
    { "skuId": 100, "skuName": "상품A", "requestedQty": 2 }
  ]
}
```

### 반품 목록 조회
```
GET /return-orders?warehouseId=1&status=REQUESTED&page=0&size=20
```

### 반품 단건 조회
```
GET /return-orders/{id}
```

### 반품 실사 처리
```
PATCH /return-orders/{id}/receive
```
Request
```json
{
  "items": [
    { "returnItemId": 1, "receivedQty": 2, "condition": "RESELLABLE" }
  ]
}
```

### 반품 완료 (→ 재고 복구)
```
PATCH /return-orders/{id}/complete
```
> RESELLABLE → availableQty 복구
> DEFECTIVE → 재고 미복구, 불량 이력만 기록

### 반품 거부
```
PATCH /return-orders/{id}/reject
```

---

## 설계 도메인 API (미구현)

### 적치
| Method | Path | 설명 |
|---|---|---|
| POST | /putaway-tasks | 적치 지시 생성 |
| PATCH | /putaway-tasks/{id}/confirm | 위치 확정 |
| GET | /putaway-tasks?status=PENDING | 미완료 조회 |

### 피킹
| Method | Path | 설명 |
|---|---|---|
| POST | /picking-waves | 웨이브 생성 |
| GET | /picking-waves/{id}/tasks | 작업 목록 |
| PATCH | /picking-tasks/{id}/pick | 피킹 완료 |

### DPS
| Method | Path | 설명 |
|---|---|---|
| POST | /dps/instructions | 점등 지시 |
| PATCH | /dps/instructions/{id}/confirm | 버튼 확인 |
| WS | /ws/dps/{locationCode} | 실시간 점등 상태 |

### 송장
| Method | Path | 설명 |
|---|---|---|
| POST | /shipping-labels | 송장 생성 |
| POST | /shipping-labels/{id}/print | 프린터 전송 |
| GET | /shipping-labels/{id}/pdf | PDF 다운로드 |
