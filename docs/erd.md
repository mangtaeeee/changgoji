# ERD — 엔티티 관계도

## 전체 관계도

```
warehouse
    │
    ├── inbound_order ──────── inbound_item
    │       └── inbound_receipt
    │
    ├── inventory ──────────── inventory_location
    │       └── inventory_history
    │
    ├── outbound_order ─────── outbound_item
    │       └── return_order ── return_item
    │
    └── (설계) putaway_task
              picking_wave ── picking_task
              dps_instruction
              shipping_label
```

---

## 구현 도메인 테이블

### inbound_order (입고 지시)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| warehouse_id | BIGINT | 창고 ID |
| supplier_id | BIGINT | 공급사 ID |
| status | VARCHAR | REQUESTED / SCHEDULED / RECEIVING / COMPLETED / CANCELLED |
| scheduled_date | DATE | 입고 예정일 |
| completed_at | TIMESTAMP | 입고 확정 시각 |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### inbound_item (입고 품목)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| inbound_order_id | BIGINT FK | inbound_order.id |
| sku_id | BIGINT | 상품 SKU ID |
| sku_name | VARCHAR | 상품명 |
| ordered_qty | INTEGER | 지시 수량 |
| received_qty | INTEGER | 실사 수량 (default 0) |
| status | VARCHAR | PENDING / PARTIAL / COMPLETED |

### inbound_receipt (입고 확인서)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| inbound_order_id | BIGINT FK (unique) | inbound_order.id |
| confirmed_by | BIGINT | 작업자 ID |
| confirmed_at | TIMESTAMP | 확정 시각 |
| memo | TEXT | 비고 |

---

### inventory (재고)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| warehouse_id | BIGINT | 창고 ID |
| sku_id | BIGINT | 상품 SKU ID |
| sku_name | VARCHAR | 상품명 |
| available_qty | INTEGER | 가용 재고 |
| allocated_qty | INTEGER | 출고 할당 재고 (default 0) |
| version | BIGINT | 낙관적 락 버전 |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

**인덱스**
- `idx_inventory_warehouse_sku (warehouse_id, sku_id)` — 창고 단위 조회 최적화

### inventory_location (적치 위치)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| inventory_id | BIGINT FK | inventory.id |
| location_code | VARCHAR | 위치 코드 (예: A-01-03) |
| qty | INTEGER | 해당 위치 수량 |
| updated_at | TIMESTAMP | |

### inventory_history (재고 이력)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| inventory_id | BIGINT FK | inventory.id |
| change_type | VARCHAR | INBOUND / OUTBOUND / ALLOCATE / RELEASE / RETURN_INBOUND / DEFECTIVE_RETURN / ADJUST |
| before_qty | INTEGER | 변경 전 수량 |
| after_qty | INTEGER | 변경 후 수량 |
| change_qty | INTEGER | 변경 수량 |
| reference_id | BIGINT | 입고/출고/반품 ID |
| created_at | TIMESTAMP | |

---

### outbound_order (출고 지시)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| warehouse_id | BIGINT | 창고 ID |
| order_id | VARCHAR | 외부 주문 ID |
| status | VARCHAR | PENDING / ALLOCATED / PICKING / SHIPPED / CANCELLED |
| requested_at | TIMESTAMP | 출고 요청 시각 |
| shipped_at | TIMESTAMP | 출고 확정 시각 |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### outbound_item (출고 품목)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| outbound_order_id | BIGINT FK | outbound_order.id |
| sku_id | BIGINT | 상품 SKU ID |
| requested_qty | INTEGER | 출고 요청 수량 |
| shipped_qty | INTEGER | 실제 출고 수량 (default 0) |
| location_code | VARCHAR | 피킹 위치 |

---

### return_order (반품 지시)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| outbound_order_id | BIGINT FK | 원 출고 지시 ID |
| warehouse_id | BIGINT | 창고 ID |
| status | VARCHAR | REQUESTED / RECEIVED / COMPLETED / REJECTED |
| reason | VARCHAR | CUSTOMER_CHANGE / DEFECT / WRONG_ITEM / OTHER |
| requested_at | TIMESTAMP | |
| completed_at | TIMESTAMP | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### return_item (반품 품목)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| return_order_id | BIGINT FK | return_order.id |
| sku_id | BIGINT | 상품 SKU ID |
| sku_name | VARCHAR | 상품명 |
| requested_qty | INTEGER | 반품 요청 수량 |
| received_qty | INTEGER | 실사 수량 (default 0) |
| condition | VARCHAR | RESELLABLE / DEFECTIVE |

---

## 설계 도메인 테이블 (요약)

### putaway_task (적치 지시)
- id, inbound_receipt_id, sku_id
- recommended_location, confirmed_location
- status: PENDING / IN_PROGRESS / COMPLETED
- assigned_to

### picking_wave (피킹 웨이브)
- id, warehouse_id, status: OPEN / IN_PROGRESS / COMPLETED

### picking_task (피킹 작업)
- id, wave_id, outbound_item_id
- location_code, sku_id, qty
- assigned_to, status: PENDING / PICKED / SKIPPED

### dps_instruction (DPS 지시)
- id, picking_task_id, location_code
- display_qty, confirmed_qty
- status: WAITING / DISPLAYING / CONFIRMED

### shipping_label (송장)
- id, outbound_order_id
- tracking_no, carrier
- label_data (JSON)
- status: PENDING / PRINTED / FAILED
- printed_at
