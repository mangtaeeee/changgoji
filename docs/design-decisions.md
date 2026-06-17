# 기술 의사결정 기록 (Design Decisions)

> 이 문서는 창고지기 개발 과정에서 내린 기술적 판단과 그 근거를 기록합니다.
> "왜 이렇게 만들었는가"에 대한 답변입니다.

---

## 1. 재고 차감 동시성 제어 — 낙관적 락 선택

### 문제
출고 지시 할당 시 동일 SKU에 동시 요청이 들어오면 재고가 중복 차감될 수 있음.

### 선택지 비교
| | 낙관적 락 (@Version) | 비관적 락 (SELECT FOR UPDATE) |
|---|---|---|
| 방식 | 충돌 시 예외 → 재시도 | 트랜잭션 시작 시 즉시 락 |
| 성능 | 충돌 적을 때 유리 | 충돌 많을 때 유리 |
| 데드락 | 없음 | 가능성 있음 |
| 대기 | 없음 | 락 해제까지 대기 |

### 결정: 낙관적 락
WMS 환경에서 동일 SKU에 동시 출고 지시가 몰리는 상황은 드뭄. 충돌 확률이 낮아 재시도 비용(OptimisticLockingFailureException → 최대 3회 retry)이 락 대기 비용보다 낮다고 판단.

```java
@Entity
public class Inventory {
    @Version
    private Long version;  // 충돌 감지용

    public void allocate(int qty) {
        if (this.availableQty < qty) throw new InsufficientStockException();
        this.availableQty -= qty;
        this.allocatedQty += qty;
    }
}

@Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3,
           backoff = @Backoff(delay = 100))
public void allocateStock(Long inventoryId, int qty) { ... }
```

---

## 2. 반품 재고 복구 — condition 분기 처리

### 문제
반품 상품이 모두 재판매 가능한 상태가 아님. 불량품을 재고로 복구하면 실제 판매 가능 재고가 부풀려짐.

### 결정: ItemCondition Enum으로 분기
```
RESELLABLE → Inventory.increase() → availableQty 복구
DEFECTIVE  → 재고 미복구 → InventoryHistory(DEFECTIVE_RETURN) 기록만
```

이력은 두 경우 모두 남겨서 반품 감사 로그 확보. 불량 이력은 향후 공급사 클레임 처리에 활용 가능.

---

## 3. 출고 취소 — 보상 트랜잭션 적용

### 문제
출고 확정 중 외부 택배사 API 호출 실패 시 내부 DB와 외부 상태가 불일치할 수 있음.

### 결정: 보상 트랜잭션
외부 API 호출은 로컬 트랜잭션과 단일 원자성으로 묶을 수 없으므로, 실패 시 이미 처리된 내부 상태를 역으로 되돌리는 보상 처리 적용.

```
출고 지시 취소 → OutboundOrder.cancel()
               → ALLOCATED 상태였으면 InventoryService.releaseStock()
               → allocatedQty → availableQty 복구
               → InventoryHistory(RELEASE) 기록
```

시큐어레터에서 외부 Agent API 연동 정합성 보완 경험을 동일하게 적용.

---

## 4. 재고 이력 — 별도 테이블 분리

### 문제
재고 수량만 보면 "왜 이 수량이 됐는지" 추적 불가. 감사 로그와 재고 역산이 필요.

### 결정: InventoryHistory 별도 테이블
모든 재고 변경(입고/출고/할당/해제/반품/조정)을 이력으로 남김.

```
changeType: INBOUND / OUTBOUND / ALLOCATE / RELEASE
          / RETURN_INBOUND / DEFECTIVE_RETURN / ADJUST
beforeQty, afterQty, changeQty, referenceId 기록
```

이벤트 소싱 패턴을 참고했으나 완전한 이벤트 소싱은 아님. 현재 규모에서는 이력 테이블로 충분하고, 추후 이벤트 스트림으로 전환 시 이 이력이 기준점이 됨.

---

## 5. 재고 조회 성능 — QueryDSL fetch join + 복합 인덱스

### 문제
창고 전체 재고 조회 시 SKU별 위치 정보(InventoryLocation)까지 N+1 발생.

### 결정: fetch join + DTO Projection
```java
queryFactory
    .select(Projections.constructor(InventoryDto.class,
        inventory.id, inventory.skuId, inventory.skuName,
        inventory.availableQty, inventoryLocation.locationCode))
    .from(inventory)
    .leftJoin(inventoryLocation).on(inventoryLocation.inventoryId.eq(inventory.id))
    .where(inventory.warehouseId.eq(warehouseId))
    .fetch();
```

엔티티 전체를 올리지 않고 필요한 컬럼만 DTO로 바로 받아 영속성 컨텍스트 부하 최소화.

### 복합 인덱스
```sql
CREATE INDEX idx_inventory_warehouse_sku ON inventory (warehouse_id, sku_id);
```

창고 단위 조회가 대부분이므로 `warehouse_id`를 선두 컬럼으로. SKU 단건 조회 시에도 `(warehouse_id, sku_id)` 복합 조건으로 커버.

---

## 6. 트랜잭션 readOnly 기본값 적용

### 결정
Service 클래스 레벨에 `@Transactional(readOnly = true)` 기본 적용, 쓰기 메서드만 `@Transactional` 개별 선언.

### 근거
readOnly 트랜잭션은 JPA flush 모드를 NEVER로 설정해 불필요한 dirty checking을 생략. 읽기 비중이 높은 WMS 특성상 전체적인 DB 부하 감소 효과.

---

## 7. 엔티티 설계 — Setter 금지 + 정적 팩토리 메서드

### 결정
- `@Setter` 사용 금지
- `@AllArgsConstructor` 금지
- 생성: 정적 팩토리 메서드 `create()` / `of()`
- 상태 변경: 도메인 메서드 (`confirm()`, `cancel()`, `allocate()` 등)

### 근거
WMS는 상태 전이가 핵심 도메인 로직. Setter를 열어두면 어디서든 상태를 임의로 바꿀 수 있어 `PENDING → SHIPPED` 같은 잘못된 전이를 막을 수 없음. 상태 변경 메서드 내부에서 유효성 검증을 함께 처리해 도메인 규칙을 엔티티가 스스로 지키도록 설계.

```java
public void ship() {
    if (this.status != OutboundStatus.PICKING) {
        throw new InvalidStatusException(ErrorCode.INVALID_STATUS);
    }
    this.status = OutboundStatus.SHIPPED;
    this.shippedAt = LocalDateTime.now();
}
```
