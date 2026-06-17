# ERD

이 문서는 현재 구현된 엔티티와 `infra/db/init.sql` 기준으로 정리한다.

---

## 전체 관계도

```
inbound_order 1 ── N inbound_item
inbound_order 1 ── 1 inbound_receipt

inventory 1 ── N inventory_location
inventory 1 ── N inventory_history

outbound_order 1 ── N outbound_item
outbound_order 1 ── N return_order
return_order 1 ── N return_item

inbound_order / inbound_item ── N putaway_task
outbound_order 1 ── N picking_wave
picking_wave 1 ── N picking_task
outbound_item 1 ── N picking_task

outbound_order 1 ── N shipping_label
```

> 현재 SKU와 Warehouse는 별도 마스터 테이블로 두지 않고 `warehouse_id`, `sku_id`, `sku_name` 값을 각 도메인에서 참조한다. 포트폴리오 MVP에서는 WMS 업무 흐름을 먼저 보여주는 것이 목적이라 상품/창고 마스터는 후순위로 둔다.

---

## inbound_order

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 입고 지시 ID |
| warehouse_id | BIGINT | 창고 ID |
| supplier_id | BIGINT | 공급사 ID |
| status | VARCHAR(30) | REQUESTED / SCHEDULED / RECEIVING / COMPLETED / CANCELLED |
| scheduled_date | DATE | 입고 예정일 |
| completed_at | TIMESTAMP | 입고 확정 시각 |
| created_at | TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | 수정 시각 |

## inbound_item

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 입고 품목 ID |
| inbound_order_id | BIGINT FK | 입고 지시 ID |
| sku_id | BIGINT | SKU ID |
| sku_name | VARCHAR(255) | 상품명 스냅샷 |
| ordered_qty | INTEGER | 입고 지시 수량 |
| received_qty | INTEGER | 실사 입고 수량 |
| status | VARCHAR(30) | PENDING / PARTIAL / COMPLETED |

## inbound_receipt

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 입고 확인서 ID |
| inbound_order_id | BIGINT FK UNIQUE | 입고 지시 ID |
| confirmed_by | BIGINT | 확정 작업자 ID |
| confirmed_at | TIMESTAMP | 확정 시각 |
| memo | TEXT | 비고 |

---

## inventory

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 재고 ID |
| warehouse_id | BIGINT | 창고 ID |
| sku_id | BIGINT | SKU ID |
| sku_name | VARCHAR(255) | 상품명 |
| available_qty | INTEGER | 출고 가능한 재고 |
| allocated_qty | INTEGER | 출고 지시에 할당된 재고 |
| version | BIGINT | 낙관적 락 버전 |
| created_at | TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | 수정 시각 |

인덱스:
- `idx_inventory_warehouse_sku (warehouse_id, sku_id)`

## inventory_location

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 위치 재고 ID |
| inventory_id | BIGINT FK | 재고 ID |
| location_code | VARCHAR(255) | 위치 코드 |
| qty | INTEGER | 해당 위치의 수량 |
| updated_at | TIMESTAMP | 수정 시각 |

인덱스:
- `idx_inventory_location_inventory_location_code (inventory_id, location_code)`

## inventory_history

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 재고 이력 ID |
| inventory_id | BIGINT FK | 재고 ID |
| change_type | VARCHAR(30) | INBOUND / OUTBOUND / ALLOCATE / RELEASE / RETURN_INBOUND / DEFECTIVE_RETURN / ADJUST |
| before_qty | INTEGER | 변경 전 기준 수량 |
| after_qty | INTEGER | 변경 후 기준 수량 |
| change_qty | INTEGER | 변경 수량 |
| reference_id | BIGINT | 입고/출고/반품 등 참조 ID |
| created_at | TIMESTAMP | 생성 시각 |

---

## outbound_order

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 출고 지시 ID |
| warehouse_id | BIGINT | 창고 ID |
| order_id | VARCHAR(255) | 외부 주문 ID |
| status | VARCHAR(30) | PENDING / ALLOCATED / PICKING / SHIPPED / CANCELLED |
| requested_at | TIMESTAMP | 출고 요청 시각 |
| shipped_at | TIMESTAMP | 출고 확정 시각 |
| created_at | TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | 수정 시각 |

## outbound_item

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 출고 품목 ID |
| outbound_order_id | BIGINT FK | 출고 지시 ID |
| sku_id | BIGINT | SKU ID |
| requested_qty | INTEGER | 출고 요청 수량 |
| shipped_qty | INTEGER | 실제 출고 수량 |
| location_code | VARCHAR(255) | 피킹 위치 |

---

## return_order

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 반품 지시 ID |
| outbound_order_id | BIGINT FK | 원 출고 지시 ID |
| warehouse_id | BIGINT | 창고 ID |
| status | VARCHAR(30) | REQUESTED / RECEIVED / COMPLETED / REJECTED |
| reason | VARCHAR(30) | CUSTOMER_CHANGE / DEFECT / WRONG_ITEM / OTHER |
| requested_at | TIMESTAMP | 반품 접수 시각 |
| completed_at | TIMESTAMP | 반품 완료 시각 |
| created_at | TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | 수정 시각 |

## return_item

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 반품 품목 ID |
| return_order_id | BIGINT FK | 반품 지시 ID |
| sku_id | BIGINT | SKU ID |
| sku_name | VARCHAR(255) | 상품명 |
| requested_qty | INTEGER | 반품 요청 수량 |
| received_qty | INTEGER | 실제 회수 수량 |
| condition | VARCHAR(30) | RESELLABLE / DEFECTIVE |

---

## putaway_task

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 적치 작업 ID |
| inbound_order_id | BIGINT FK | 입고 지시 ID |
| inbound_item_id | BIGINT FK | 입고 품목 ID |
| warehouse_id | BIGINT | 창고 ID |
| sku_id | BIGINT | SKU ID |
| sku_name | VARCHAR(255) | 상품명 |
| qty | INTEGER | 적치 대상 수량 |
| recommended_location | VARCHAR(255) | 추천 위치 |
| confirmed_location | VARCHAR(255) | 확정 위치 |
| status | VARCHAR(30) | PENDING / IN_PROGRESS / COMPLETED |
| assigned_to | BIGINT | 작업자 ID |
| created_at | TIMESTAMP | 생성 시각 |
| completed_at | TIMESTAMP | 완료 시각 |

인덱스:
- `idx_putaway_warehouse_status (warehouse_id, status)`

---

## picking_wave

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 피킹 웨이브 ID |
| warehouse_id | BIGINT | 창고 ID |
| outbound_order_id | BIGINT FK | 출고 지시 ID |
| status | VARCHAR(30) | OPEN / IN_PROGRESS / COMPLETED |
| created_at | TIMESTAMP | 생성 시각 |
| completed_at | TIMESTAMP | 완료 시각 |

인덱스:
- `idx_picking_wave_warehouse_status (warehouse_id, status)`

## picking_task

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 피킹 작업 ID |
| wave_id | BIGINT FK | 피킹 웨이브 ID |
| outbound_item_id | BIGINT FK | 출고 품목 ID |
| sku_id | BIGINT | SKU ID |
| location_code | VARCHAR(255) | 피킹 위치 |
| qty | INTEGER | 피킹 수량 |
| assigned_to | BIGINT | 작업자 ID |
| status | VARCHAR(30) | PENDING / PICKED / SKIPPED |
| picked_at | TIMESTAMP | 피킹 완료 시각 |

---

## shipping_label

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL PK | 송장 ID |
| outbound_order_id | BIGINT FK | 출고 지시 ID |
| tracking_no | VARCHAR(255) UNIQUE | 운송장 번호 |
| carrier | VARCHAR(255) | 택배사 |
| receiver_name | VARCHAR(255) | 수령인 |
| receiver_phone | VARCHAR(255) | 수령인 연락처 |
| receiver_address | VARCHAR(255) | 배송지 |
| label_data | TEXT | 출력용 송장 데이터 |
| status | VARCHAR(30) | PENDING / PRINT_REQUESTED / PRINTED / FAILED |
| created_at | TIMESTAMP | 생성 시각 |
| print_requested_at | TIMESTAMP | 출력 요청 시각 |
| printed_at | TIMESTAMP | 출력 완료 시각 |
| failure_reason | VARCHAR(255) | 출력 실패 사유 |

인덱스:
- `idx_shipping_label_outbound_order (outbound_order_id)`

---

## 설계만 남은 테이블

### dps_instruction

아직 구현 전이다. 피킹 작업을 기준으로 점등 위치와 수량을 전달하는 시뮬레이터를 만든 뒤 추가한다.

예상 컬럼:
- id
- picking_task_id
- location_code
- display_qty
- confirmed_qty
- status: WAITING / DISPLAYING / CONFIRMED
