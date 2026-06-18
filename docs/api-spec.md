# API 명세

> Base URL: `http://localhost:8080/api/v1`  
> Swagger UI: `http://localhost:8080/swagger-ui.html`

현재 문서는 실제 구현된 Spring Boot API를 기준으로 정리한다. 목록 조회, 검색 조건, 권한 분리처럼 화면 개발 단계에서 필요한 API는 이후 `admin-api`, `pda-api`로 분리하며 확장한다.

---

## 공통 응답

### 성공
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {}
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

### ErrorCode

| 코드 | 메시지 | HTTP 상태 |
|---|---|---|
| INVENTORY_001 | 재고가 부족합니다. | 400 |
| ORDER_001 | 유효하지 않은 상태 전이입니다. | 400 |
| COMMON_001 | 동시 요청으로 인해 처리에 실패했습니다. 다시 시도해주세요. | 409 |
| COMMON_002 | 입력값이 올바르지 않습니다. | 400 |
| COMMON_999 | 서버 내부 오류가 발생했습니다. | 500 |
| INBOUND_001 | 입고 지시를 찾을 수 없습니다. | 404 |
| OUTBOUND_001 | 출고 지시를 찾을 수 없습니다. | 404 |
| RETURN_001 | 반품 지시를 찾을 수 없습니다. | 404 |
| PUTAWAY_001 | 적치 작업을 찾을 수 없습니다. | 404 |
| PICKING_001 | 피킹 웨이브를 찾을 수 없습니다. | 404 |
| PICKING_002 | 피킹 작업을 찾을 수 없습니다. | 404 |
| SHIPPING_001 | 송장을 찾을 수 없습니다. | 404 |

> 각 도메인 예외는 `BusinessException`을 상속하고, HTTP 상태는 `ErrorCode`에 정의한다.

---

## 입고 API

### 입고 지시 등록

`POST /inbound-orders`

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

### 입고 지시 단건 조회

`GET /inbound-orders/{id}`

응답에는 실사 처리에 필요한 `inboundItemId`가 포함된다.

### 실사 입고 처리

`PATCH /inbound-orders/{id}/receive`

```json
{
  "items": [
    { "inboundItemId": 1, "receivedQty": 48 },
    { "inboundItemId": 2, "receivedQty": 30 }
  ]
}
```

### 입고 확정

`PATCH /inbound-orders/{id}/confirm`

```json
{
  "confirmedBy": 999,
  "memo": "일부 파손 1개 제외"
}
```

처리 결과:
- 실사 수량 기준으로 `Inventory.availableQty` 증가
- `InventoryHistory(INBOUND)` 기록
- 입고 품목별 `PutawayTask` 자동 생성
- `InboundReceipt` 저장

---

## 재고 API

### 창고 재고 목록 조회

`GET /inventories?warehouseId=1&page=0&size=50`

창고 ID 기준으로 SKU별 재고와 위치별 수량을 페이지 단위로 조회한다. QueryDSL DTO Projection을 사용한다.

- `page` 기본값: 0
- `size` 기본값: 50
- `size` 최대값: 200

```json
{
  "items": [
    {
      "id": 1,
      "warehouseId": 1,
      "skuId": 100,
      "skuName": "상품A",
      "availableQty": 48,
      "allocatedQty": 5,
      "locationCode": "A-01-03",
      "locationQty": 43
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 20000,
  "totalPages": 400
}
```

### SKU별 재고 조회

`GET /inventories/{skuId}?warehouseId=1`

### 재고 조정

`POST /inventories/adjust`

```json
{
  "inventoryId": 1,
  "adjustQty": -2,
  "reason": "파손으로 인한 폐기"
}
```

처리 결과:
- `Inventory.availableQty` 조정
- `InventoryHistory(ADJUST)` 기록

### 재고 이력 조회

`GET /inventories/{id}/history`

재고 ID 기준으로 입고, 할당, 출고, 반품, 조정 이력을 최신순으로 조회한다.

---

## 적치 API

입고 확정 시 자동 생성된 적치 작업을 조회하고, 작업자가 실제 위치를 확정하는 API다.

### 적치 작업 목록 조회

`GET /putaway-tasks?warehouseId=1&status=PENDING`

### 적치 작업 단건 조회

`GET /putaway-tasks/{id}`

### 적치 작업 시작

`PATCH /putaway-tasks/{id}/start`

```json
{
  "assignedTo": 999
}
```

### 적치 확정

`PATCH /putaway-tasks/{id}/confirm`

```json
{
  "confirmedLocation": "A-01-03"
}
```

처리 결과:
- `PutawayTask.status = COMPLETED`
- `InventoryLocation.qty` 증가

---

## 출고 API

### 출고 지시 등록

`POST /outbound-orders`

```json
{
  "warehouseId": 1,
  "orderId": "ORDER-20260617-001",
  "items": [
    { "skuId": 100, "requestedQty": 5, "locationCode": "A-01-03" }
  ]
}
```

### 출고 지시 단건 조회

`GET /outbound-orders/{id}`

응답에는 피킹/출고 확인에 필요한 `outboundItemId`가 포함된다.

### 재고 할당

`PATCH /outbound-orders/{id}/allocate`

처리 결과:
- `availableQty` 감소
- `allocatedQty` 증가
- `InventoryHistory(ALLOCATE)` 기록
- `PickingWave`, `PickingTask` 자동 생성

### 출고 확정

`PATCH /outbound-orders/{id}/ship`

처리 결과:
- `allocatedQty` 감소
- 출고 품목 `shippedQty` 반영
- `InventoryHistory(OUTBOUND)` 기록

### 출고 취소

`PATCH /outbound-orders/{id}/cancel`

처리 결과:
- `ALLOCATED` 상태이면 `allocatedQty → availableQty` 복구
- `InventoryHistory(RELEASE)` 기록

---

## 피킹 API

출고 할당 후 자동 생성된 피킹 웨이브와 위치별 작업을 처리한다.

### 피킹 웨이브 목록 조회

`GET /picking-waves?warehouseId=1&status=OPEN`

### 피킹 웨이브 단건 조회

`GET /picking-waves/{id}`

### 피킹 작업 목록 조회

`GET /picking-waves/{id}/tasks`

### 피킹 완료

`PATCH /picking-tasks/{id}/pick`

```json
{
  "assignedTo": 999
}
```

처리 결과:
- `PickingTask.status = PICKED`
- `InventoryLocation.qty` 감소
- 모든 작업이 끝나면 `PickingWave.status = COMPLETED`

---

## 반품 API

### 반품 접수

`POST /return-orders`

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

### 반품 단건 조회

`GET /return-orders/{id}`

응답에는 실사 처리에 필요한 `returnItemId`가 포함된다.

### 반품 실사 처리

`PATCH /return-orders/{id}/receive`

```json
{
  "items": [
    { "returnItemId": 1, "receivedQty": 2, "condition": "RESELLABLE" }
  ]
}
```

### 반품 완료

`PATCH /return-orders/{id}/complete`

처리 결과:
- `RESELLABLE`: `availableQty` 복구, `InventoryHistory(RETURN_INBOUND)` 기록
- `DEFECTIVE`: 재고 미복구, `InventoryHistory(DEFECTIVE_RETURN)` 기록

### 반품 거부

`PATCH /return-orders/{id}/reject`

---

## 송장 API

### 송장 생성

`POST /shipping-labels`

```json
{
  "outboundOrderId": 1,
  "carrier": "CJ대한통운",
  "receiverName": "홍길동",
  "receiverPhone": "010-1234-5678",
  "receiverAddress": "서울시 강남구 테헤란로 123"
}
```

처리 결과:
- 운송장 번호 생성
- 출력용 `labelData` 저장
- 상태 `PENDING`

### 송장 단건 조회

`GET /shipping-labels/{id}`

### 출고 지시별 송장 조회

`GET /shipping-labels?outboundOrderId=1`

### 송장 출력 요청

`POST /shipping-labels/{id}/print`

상태를 `PRINT_REQUESTED`로 변경한다. 추후 `print-agent`가 이 상태의 송장을 가져가 출력한다.

### 송장 출력 성공 처리

`PATCH /shipping-labels/{id}/printed`

### 송장 출력 실패 처리

`PATCH /shipping-labels/{id}/failed`

```json
{
  "failureReason": "프린터 연결 실패"
}
```

---

## 아직 구현하지 않은 범위

| 도메인 | 계획 |
|---|---|
| DPS | 피킹 작업 기반 점등 지시와 시뮬레이터 구현 예정 |
| 송장 PDF 다운로드 | 현재는 labelData와 출력 상태만 관리. PDF 렌더링은 다음 단계 |
| 관리자/작업자 API 분리 | 현재는 `/api/v1` 공통 API. 화면 개발 시 `/admin-api/v1`, `/pda-api/v1`로 분리 예정 |
