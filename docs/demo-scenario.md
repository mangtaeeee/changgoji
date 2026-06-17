# 시연 시나리오

이 문서는 Swagger 또는 REST Client로 창고지기의 핵심 WMS 흐름을 확인하기 위한 순서입니다.

Base URL:

```text
http://localhost:8080/api/v1
```

실행 전 서버를 먼저 올립니다.

```powershell
cd infra
docker compose up --build
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

---

## 1. 입고 지시 생성

공급사에서 상품이 들어올 예정이라는 지시를 등록합니다.

```http
POST /inbound-orders
```

예상 결과:
- `InboundOrder.status = REQUESTED`
- 응답의 `items[0].id`를 이후 실사 입고에서 사용

---

## 2. 실사 입고 처리

입고 예정 수량과 실제 도착 수량이 다를 수 있으므로, 현장에서 확인한 수량을 반영합니다.

```http
PATCH /inbound-orders/{inboundOrderId}/receive
```

예상 결과:
- 입고 지시 상태가 `RECEIVING`으로 변경
- 입고 품목의 `receivedQty` 반영

---

## 3. 입고 확정

실사 수량을 기준으로 입고를 확정합니다.

```http
PATCH /inbound-orders/{inboundOrderId}/confirm
```

예상 결과:
- `Inventory.availableQty` 증가
- `InventoryHistory(INBOUND)` 기록
- `PutawayTask` 자동 생성
- `InboundReceipt` 저장

---

## 4. 재고 조회

입고 확정 후 재고가 증가했는지 확인합니다.

```http
GET /inventories/{skuId}?warehouseId=1
GET /inventories?warehouseId=1
GET /inventories/{inventoryId}/history
```

예상 결과:
- `availableQty`가 실사 수량만큼 증가
- 재고 이력에 `INBOUND` 기록 존재

---

## 5. 적치 작업 조회 및 확정

입고된 재고를 실제 창고 위치에 넣는 작업입니다.

```http
GET /putaway-tasks?warehouseId=1&status=PENDING
PATCH /putaway-tasks/{putawayTaskId}/start
PATCH /putaway-tasks/{putawayTaskId}/confirm
```

예상 결과:
- 적치 작업 상태가 `COMPLETED`로 변경
- `InventoryLocation.qty` 증가

---

## 6. 출고 지시 생성

외부 주문을 기준으로 출고 지시를 생성합니다.

```http
POST /outbound-orders
```

예상 결과:
- `OutboundOrder.status = PENDING`
- 응답의 `items[0].id`를 피킹 작업과 연결할 수 있음

---

## 7. 재고 할당

출고 요청 수량만큼 가용 재고를 주문에 묶어둡니다.

```http
PATCH /outbound-orders/{outboundOrderId}/allocate
```

예상 결과:
- `availableQty` 감소
- `allocatedQty` 증가
- `InventoryHistory(ALLOCATE)` 기록
- `PickingWave`, `PickingTask` 자동 생성

---

## 8. 피킹 작업 조회 및 완료

작업자가 지정 위치에서 상품을 집는 단계입니다.

```http
GET /picking-waves?warehouseId=1&status=OPEN
GET /picking-waves/{pickingWaveId}/tasks
PATCH /picking-tasks/{pickingTaskId}/pick
```

예상 결과:
- 피킹 작업 상태가 `PICKED`로 변경
- 위치 재고 수량 감소
- 모든 작업이 끝나면 피킹 웨이브가 `COMPLETED`로 변경

---

## 9. 송장 생성 및 출력 요청

출고 지시에 대한 송장 데이터를 생성하고 출력 요청 상태로 변경합니다.

```http
POST /shipping-labels
POST /shipping-labels/{shippingLabelId}/print
PATCH /shipping-labels/{shippingLabelId}/printed
```

예상 결과:
- 송장 번호 생성
- 상태가 `PENDING → PRINT_REQUESTED → PRINTED`로 변경

---

## 10. 출고 확정

실제 출고를 확정하고 할당 재고를 차감합니다.

```http
PATCH /outbound-orders/{outboundOrderId}/ship
```

예상 결과:
- `allocatedQty` 감소
- `InventoryHistory(OUTBOUND)` 기록
- 출고 지시 상태가 `SHIPPED`로 변경

---

## 11. 반품 접수, 실사, 완료

출고된 상품이 반품되는 흐름입니다.

```http
POST /return-orders
PATCH /return-orders/{returnOrderId}/receive
PATCH /return-orders/{returnOrderId}/complete
```

예상 결과:
- `RESELLABLE`이면 `availableQty` 복구
- `DEFECTIVE`이면 재고 복구 없이 `InventoryHistory(DEFECTIVE_RETURN)` 기록

---

## 시연에서 강조할 점

- 입고 확정과 재고 증가를 분리하지 않고 하나의 트랜잭션 흐름으로 처리한다.
- 입고 후 바로 위치 재고를 늘리지 않고, 적치 작업 완료 시 위치 수량을 반영한다.
- 출고는 가용 재고를 바로 없애지 않고 할당 재고로 이동시킨다.
- 피킹은 출고 지시와 별도 작업으로 관리한다.
- 반품은 상품 상태에 따라 재고 복구 여부가 달라진다.
- 모든 재고 변경은 이력으로 추적한다.
