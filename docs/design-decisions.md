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

---

## 8. 사용자 인터페이스 방향 — Android PDA 앱 + 관리자 웹 분리

### 문제
WMS는 단순히 API만 구현하면 현장 흐름이 잘 드러나지 않음. 입고 실사, 적치, 피킹, 반품 검수는 작업자가 창고 안에서 이동하면서 처리하는 업무이고, 관리자/운영자는 지시 생성과 현황 모니터링을 데스크톱 화면에서 처리함.

참고한 커머스 포트폴리오 프로젝트는 고객 웹, 네이티브 앱, 관리자 웹, 백엔드를 실행 단위로 분리해 백엔드 API가 실제 화면에서 어떻게 사용되는지 보여줌. 창고지기는 같은 접근을 WMS 도메인에 맞게 재해석한다.

### 결정: Android PDA 앱과 Admin Web을 분리

```
admin-web    → 운영자: 입고/출고/반품 지시 생성, 재고 현황, 작업 진행률 조회
android-app  → 현장 작업자: 바코드 스캔, 입고 실사, 적치, 피킹, 반품 검수
backend      → WMS 도메인 API와 동시성/이력/상태 전이 처리
```

### 근거
- 물류센터 현장 단말은 Android 기반 PDA/러기드 단말 사용 사례가 많음.
- Android 앱은 바코드 스캔, 카메라, 진동, 오프라인 임시 저장 같은 현장 기능을 표현하기 좋음.
- 관리자 웹은 대량 조회, 필터링, 엑셀/송장/작업 현황처럼 데스크톱에 적합한 업무를 담당.
- 포트폴리오 관점에서 백엔드 API가 실제 운영자/작업자 화면으로 소비되는 모습을 보여줄 수 있음.

---

## 9. API 경계 — 관리자 API와 작업자 API 분리

### 문제
운영자와 현장 작업자는 같은 WMS 데이터를 보지만 목적과 권한이 다름. 하나의 API 응답 모델로 모두 처리하면 화면별 요구사항이 섞이고, 권한 검증도 불명확해질 수 있음.

### 결정: API 경계를 역할 기준으로 분리

```
/admin-api/v1/**  → 관리자 웹용 API
/pda-api/v1/**    → Android PDA 앱용 API
/api/v1/**        → 초기 개발 및 공통 도메인 API, 추후 역할별 API로 흡수
```

### 근거
- 관리자 API는 목록 조회, 통계, 필터링, 작업 배정, 이력 조회 중심.
- PDA API는 단건 작업 처리, 스캔 검증, 다음 작업 조회, 상태 변경 중심.
- 같은 도메인 서비스는 재사용하되 Controller/DTO를 분리하면 권한과 응답 모델을 명확히 유지할 수 있음.

---

## 10. 설계만 기능의 구현 범위 — Putaway, Picking, DPS, Shipping Label 구현

### 문제
입고/재고/출고/반품만 구현하면 기본 WMS 흐름은 성립하지만, 실제 물류센터 업무의 차별점인 적치, 피킹, 장비 연동, 송장 출력이 빠져 포트폴리오 설득력이 약해질 수 있음.

### 결정: 설계만 기능도 구현 대상으로 전환

| 도메인 | 구현 방향 |
|---|---|
| Putaway | 입고 확정 후 적치 작업 생성, 추천 위치/확정 위치 관리 |
| Picking | 출고 할당 후 피킹 웨이브 생성, 작업자별 피킹 태스크 처리 |
| DPS | 실제 장비 대신 TCP/WebSocket 기반 DPS 시뮬레이터 또는 dps-agent 구현 |
| 송장 출력 | PDF 송장 생성, 출력 큐 등록, print-agent 시뮬레이션 |

### 구현 우선순위
1. Returns 구현 완료
2. Putaway 구현
3. Picking 구현
4. Android PDA 앱에서 입고/적치/피킹/반품 작업 처리
5. Shipping Label PDF 생성
6. DPS/Print Agent 시뮬레이터 구현

### 근거
모든 기능을 실제 장비 수준으로 구현하지 않아도, 도메인 이벤트 흐름과 외부 시스템 연동 경계를 코드로 보여주면 충분히 강한 백엔드 포트폴리오가 됨.

---

## 11. DPS 연동 — 실제 장비 대신 에이전트/시뮬레이터 우선

### 문제
DPS(Digital Picking System)는 물류센터 장비와 TCP/IP 또는 전용 프로토콜로 통신해야 하지만, 개인 포트폴리오 환경에서 실제 장비를 준비하기 어려움.

### 결정: dps-agent 시뮬레이터 구현

```
backend → DPS 지시 생성
        → dps-agent로 점등 지시 전송
        → WebSocket/SSE로 관리자 화면 또는 시뮬레이터 UI에 표시
        → 작업자 확인 이벤트 수신
        → DPS 지시 CONFIRMED 처리
```

### 근거
- 실제 장비가 없어도 외부 시스템 연동 경계, 재시도, 실패 처리, 상태 동기화 문제를 표현할 수 있음.
- TCP 프로토콜을 직접 흉내 내거나 WebSocket 기반 시뮬레이터 UI를 제공하면 시연 가능성이 높음.
- 추후 실제 장비 연동 시 backend는 동일한 인터페이스를 유지하고 agent 구현만 교체할 수 있음.

---

## 12. 송장 출력 — PDF 생성과 출력 Agent 분리

### 문제
서버가 직접 프린터를 제어하면 OS/드라이버/네트워크 프린터 설정에 강하게 묶임. 실제 운영 환경에서도 API 서버와 출력 장비가 같은 네트워크 또는 같은 OS에 있다고 보장할 수 없음.

### 결정: Shipping Label과 Print Agent 분리

```
backend      → 송장 PDF 생성, 출력 요청 상태 관리
print-agent  → 출력 큐 polling 또는 push 수신, 로컬 프린터로 전송
```

### 상태 모델
```
PENDING → PRINT_REQUESTED → PRINTED
                         ↘ FAILED
```

### 근거
- API 서버는 송장 데이터와 PDF 생성, 출력 요청 이력을 책임짐.
- print-agent는 Windows 또는 프린터가 연결된 환경에 배포 가능한 독립 실행 단위로 둠.
- 포트폴리오에서는 실제 프린터 대신 출력 성공/실패를 시뮬레이션하고, PDF 다운로드로 결과를 검증함.

---

## 13. Android 앱의 역할 — 작업자 경험 중심으로 제한

### 문제
Android 앱에 관리자 기능까지 모두 넣으면 앱 범위가 커지고, WMS 현장 앱이라는 목적이 흐려질 수 있음.

### 결정: Android 앱은 작업자 PDA 기능에 집중

### 주요 화면
- 로그인 / 작업자 선택
- 오늘의 작업 목록
- 입고 실사: 입고 지시 조회 → SKU 스캔 → 실사 수량 입력
- 적치: 추천 위치 확인 → 로케이션 스캔 → 적치 확정
- 피킹: 피킹 작업 조회 → 위치 스캔 → SKU 스캔 → 수량 확인
- 반품 검수: 반품 품목 스캔 → 상태(RESELLABLE/DEFECTIVE) 선택

### 근거
Android 앱은 현장 업무의 이동성과 스캔 중심 UX를 보여주는 데 집중하고, 관리자성 업무는 admin-web으로 분리하는 것이 역할 경계가 명확함.

---

## 14. 연관관계 설계 — ManyToMany 대신 연관 엔티티 사용

### 문제
입고 지시와 SKU, 출고 지시와 SKU, 반품 지시와 SKU는 개념적으로 다대다 관계처럼 보일 수 있음. 하지만 단순 매핑 테이블로 처리하면 지시 수량, 실사 수량, 출고 수량, 상품 상태 같은 업무 속성을 담기 어려움.

### 결정: `@ManyToMany` 사용 금지, 연관 엔티티로 해소

```
InboundOrder  1:N  InboundItem   N:1  SKU(외부 식별자)
OutboundOrder 1:N  OutboundItem  N:1  SKU(외부 식별자)
ReturnOrder   1:N  ReturnItem    N:1  SKU(외부 식별자)
```

현재 SKU는 별도 상품 마스터 엔티티를 두지 않고 `skuId`, `skuName` 스냅샷으로 관리한다. 추후 상품 마스터가 생기더라도 `InboundItem`, `OutboundItem`, `ReturnItem`은 단순 조인 테이블이 아니라 수량과 상태를 가진 도메인 엔티티로 유지한다.

### 근거
- WMS의 지시 품목은 단순 연결 정보가 아니라 업무 상태와 수량을 가진 독립 도메인임.
- `@ManyToMany`는 중간 테이블의 추가 컬럼을 다루기 어렵고, 상태 전이 메서드를 넣기 부적합함.
- 연관 엔티티를 사용하면 `receive()`, `ship()` 같은 품목 단위 도메인 메서드를 둘 수 있음.

---

## 15. N+1 대응 — Batch Size 기본값 + QueryDSL fetch join

### 문제
입고/출고/반품 지시를 조회한 뒤 품목 목록을 순회하면 지시 1건 조회 후 품목 조회 쿼리가 추가로 발생할 수 있음. 목록 조회에서는 여러 지시의 품목을 접근할 때 N+1 문제가 커질 수 있음.

### 결정
- `spring.jpa.properties.hibernate.default_batch_fetch_size=100` 적용
- `@BatchSize(size = 100)`을 지시 엔티티의 품목 컬렉션에 명시
- 품목을 반드시 순회하는 처리 API는 QueryDSL fetch join repository 사용

```
InboundOrderQueryRepository.findByIdWithItems()
OutboundOrderQueryRepository.findByIdWithItems()
ReturnOrderQueryRepository.findByIdWithItems()
```

### 근거
단건 처리 API에서는 fetch join으로 지시와 품목을 한 번에 로딩해 지연 로딩 추가 쿼리를 줄인다. 목록 조회나 여러 지시를 한 번에 다루는 화면에서는 batch size가 IN 쿼리 기반으로 컬렉션 로딩을 묶어 N+1 영향을 줄인다.
