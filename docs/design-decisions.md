# 기술 의사결정 기록

이 문서는 창고지기를 만들면서 실제로 고민했던 선택과 이유를 남긴다.  
나중에 면접에서 “왜 이렇게 만들었나요?”라는 질문을 받았을 때, 코드만 보고는 잘 드러나지 않는 배경을 설명하기 위한 문서다.

---

## 1. 이 프로젝트를 WMS로 잡은 이유

배송&물류팀 JD를 보고 프로젝트 방향을 정했다. JD에서 반복해서 보이는 키워드는 OMS/WMS, 입고, 출고, 반품, 재고, 정산, 현장 데이터 모델링, 장애 대응, 성능 최적화, 문서화였다.

처음부터 커머스 전체를 만들면 주문, 상품, 결제, 회원까지 범위가 너무 넓어진다. 대신 물류팀과 직접 연결되는 WMS에 집중하면 재고 정합성, 작업 상태, 외부 장비 연동 같은 백엔드 주제를 더 깊게 보여줄 수 있다고 판단했다.

현재 창고지기는 아래 흐름을 목표로 한다.

```
입고 → 재고 반영 → 적치 → 출고 할당 → 피킹 → 송장 출력 → 출고 확정 → 반품
```

---

## 2. Android PDA 앱과 관리자 웹을 분리하기로 한 이유

WMS는 사용자 역할이 꽤 뚜렷하다. 운영자는 책상에서 지시를 만들고 현황을 본다. 작업자는 창고 안에서 상품과 위치를 스캔하면서 움직인다. 이 둘을 하나의 화면으로 묶으면 실제 업무 흐름이 잘 보이지 않는다.

그래서 화면은 나중에 아래처럼 나눌 계획이다.

```
admin-web    → 운영자: 지시 생성, 재고 조회, 작업 현황 확인
android-app  → 작업자: 입고 실사, 적치, 피킹, 반품 검수
backend      → 상태 전이, 재고 정합성, 이력 기록
```

iOS보다 Android를 생각한 이유도 여기에 있다. 물류 현장에서 쓰는 PDA나 러기드 단말은 Android 기반인 경우가 많고, 바코드 스캔/카메라/진동 같은 현장 기능을 표현하기도 자연스럽다.

---

## 3. 도메인 단위 패키지 구조

처음부터 기능별로 `controller`, `service`, `repository`를 한 폴더에 몰아넣으면 도메인이 늘어날수록 읽기 어려워진다. WMS는 입고, 재고, 출고, 반품, 적치, 피킹처럼 업무 흐름이 도메인 단위로 나뉘기 때문에 패키지도 도메인 기준으로 나눴다.

```
com.warehouse
├── inbound
├── inventory
├── outbound
├── returns
├── putaway
├── picking
└── shipping
```

공통 응답, 예외, 설정만 `common`에 둔다. 이 구조 덕분에 기능을 하나씩 추가할 때 영향 범위를 비교적 작게 유지할 수 있었다.

---

## 4. Entity에 Setter를 열지 않은 이유

WMS는 상태 전이가 중요하다. 예를 들어 출고 지시가 `PENDING`에서 바로 `SHIPPED`로 가면 재고 할당과 피킹이 빠져버린다. Setter를 열어두면 이런 잘못된 변경을 막기 어렵다.

그래서 엔티티는 아래 규칙을 지킨다.

- `@Setter` 사용하지 않음
- 생성은 정적 팩토리 메서드 사용
- 상태 변경은 `confirm()`, `allocate()`, `ship()`, `receive()` 같은 도메인 메서드로 처리

```java
public void allocate(int qty) {
    if (availableQty < qty) {
        throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
    }
    this.availableQty -= qty;
    this.allocatedQty += qty;
}
```

아직 모든 상태 전이에 대한 검증이 완벽한 것은 아니다. 지금은 흐름을 먼저 구현했고, 다음 단계에서 각 도메인 메서드 안에 상태 검증을 더 촘촘히 넣을 계획이다.

---

## 5. 재고를 availableQty와 allocatedQty로 나눈 이유

재고를 하나의 수량으로만 관리하면 주문이 들어왔을 때 문제가 생긴다. 주문 A가 재고 5개를 잡아둔 상태에서 주문 B도 같은 재고를 사용할 수 있기 때문이다.

그래서 재고를 두 가지로 나눴다.

```
availableQty = 아직 출고 지시에 잡히지 않은 수량
allocatedQty = 출고 지시에 이미 잡힌 수량
```

출고 할당 시:

```
availableQty 감소
allocatedQty 증가
```

출고 확정 시:

```
allocatedQty 감소
```

출고 취소 시:

```
allocatedQty 감소
availableQty 증가
```

이 구조가 있어야 “현재 팔 수 있는 재고”와 “주문에 잡힌 재고”를 분리해서 설명할 수 있다.

---

## 6. 재고 동시성은 낙관적 락부터 적용

동일 SKU에 출고 할당 요청이 동시에 들어오면 재고가 중복 차감될 수 있다. 이를 막기 위해 `Inventory`에 `@Version`을 두고 낙관적 락을 적용했다.

비관적 락도 선택지는 될 수 있지만, 포트폴리오 MVP에서는 먼저 낙관적 락으로 충돌을 감지하는 쪽을 선택했다.

| 구분 | 낙관적 락 | 비관적 락 |
|---|---|---|
| 방식 | 커밋 시점에 충돌 감지 | 조회 시점에 락 획득 |
| 장점 | 락 대기 비용이 적음 | 충돌이 많은 상황에 강함 |
| 단점 | 충돌 시 재시도 필요 | 대기와 데드락 가능성 |

현재 구현:
- `Inventory.version` 필드로 충돌 감지
- `OptimisticLockingFailureException`은 409 응답으로 처리

아직 자동 재시도는 넣지 않았다. 실제 운영 수준으로 올린다면 출고 할당 메서드에 제한된 횟수의 retry를 추가할 계획이다.

---

## 7. 재고 이력을 별도 테이블로 둔 이유

재고 수량만 저장하면 “왜 이 수량이 됐는지”를 알 수 없다. 물류 시스템에서는 수량 자체보다 그 수량이 만들어진 과정이 더 중요할 때가 많다.

그래서 모든 재고 변경은 `InventoryHistory`에 기록한다.

```
INBOUND
ALLOCATE
OUTBOUND
RELEASE
RETURN_INBOUND
DEFECTIVE_RETURN
ADJUST
```

완전한 이벤트 소싱은 아니다. 지금 규모에서는 이력 테이블이 더 단순하고 구현 속도도 빠르다. 대신 나중에 이벤트 기반 구조로 바꾸더라도 이 테이블이 기준 데이터가 될 수 있다.

---

## 8. ManyToMany 대신 연관 엔티티를 사용한 이유

입고 지시와 SKU, 출고 지시와 SKU, 반품 지시와 SKU는 겉으로 보면 다대다처럼 보인다. 하지만 중간 관계에 수량과 상태가 들어간다.

예를 들면:
- 입고 품목: 지시 수량, 실사 수량, 품목 상태
- 출고 품목: 요청 수량, 출고 수량, 피킹 위치
- 반품 품목: 요청 수량, 회수 수량, 상품 상태

그래서 `@ManyToMany`를 쓰지 않고 아래처럼 연관 엔티티로 풀었다.

```
InboundOrder  1:N  InboundItem
OutboundOrder 1:N  OutboundItem
ReturnOrder   1:N  ReturnItem
```

이 방식이 조금 더 코드가 많아지긴 하지만, WMS에서는 중간 엔티티 자체가 업무의 핵심이기 때문에 더 맞는 선택이라고 봤다.

---

## 9. N+1 문제 대응 방식

지시를 조회한 뒤 품목 목록을 순회하는 API가 많다. 그대로 두면 주문 1건 조회 후 품목 조회 쿼리가 추가로 나갈 수 있다.

현재는 두 가지 방식으로 대응했다.

1. 기본 배치 사이즈 설정

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

2. 품목을 반드시 순회하는 단건 처리에는 QueryDSL fetch join 사용

```
InboundOrderQueryRepository.findByIdWithItems()
OutboundOrderQueryRepository.findByIdWithItems()
ReturnOrderQueryRepository.findByIdWithItems()
```

재고 목록과 재고 이력 조회는 QueryDSL DTO Projection으로 구현했다. 엔티티를 그대로 반환하지 않고 화면에 필요한 필드만 조회해서 영속성 컨텍스트에 불필요한 객체를 올리지 않기 위해서다. 페이지네이션과 검색 조건은 관리자 화면을 붙이면서 확장할 계획이다.

---

## 10. 입고 확정 후 적치 작업을 자동 생성한 이유

입고가 확정되면 재고 수량은 늘어나지만, 현장에서는 상품을 실제 위치에 넣는 작업이 남아 있다. 이 단계를 빼면 재고는 있는데 어디 있는지 모르는 상태가 된다.

그래서 입고 확정 시 아래 일을 같이 한다.

```
실사 수량 기준 재고 증가
InventoryHistory(INBOUND) 기록
PutawayTask 생성
```

적치 확정 시에는 `InventoryLocation`에 위치별 수량을 반영한다. 이 구조가 있어야 다음 출고/피킹에서 작업자가 어느 위치로 가야 하는지 설명할 수 있다.

---

## 11. 출고 할당 후 피킹 웨이브를 자동 생성한 이유

출고 지시는 주문의 요청이고, 피킹은 현장 작업자의 실제 작업이다. 둘을 같은 개념으로 두면 나중에 작업자 배정, 작업 상태, DPS 연동을 표현하기 어렵다.

그래서 출고 할당이 성공하면 피킹 웨이브와 피킹 작업을 생성한다.

```
OutboundOrder.allocate()
→ PickingWave 생성
→ OutboundItem 기준 PickingTask 생성
```

피킹 완료 시에는 위치별 재고(`InventoryLocation.qty`)를 차감한다. 전체 재고의 출고 확정과 위치 재고의 피킹 완료를 분리해서, “주문에 잡힌 재고”와 “창고 위치에서 실제로 집은 재고”를 따로 설명할 수 있게 했다.

---

## 12. 반품은 상품 상태에 따라 재고 복구를 나눈다

반품된 상품이 모두 다시 판매 가능한 것은 아니다. 단순히 반품 수량만큼 재고를 늘리면 실제 판매 가능 재고가 부풀려진다.

그래서 반품 실사 시 `ItemCondition`을 받는다.

```
RESELLABLE → availableQty 복구, RETURN_INBOUND 이력
DEFECTIVE  → 재고 복구 없음, DEFECTIVE_RETURN 이력
```

불량 반품도 이력을 남기는 이유는 나중에 공급사 클레임이나 품질 문제 분석에 쓸 수 있기 때문이다.

---

## 13. 송장 출력은 API 서버와 출력 Agent를 분리한다

서버가 직접 프린터를 제어하면 OS, 드라이버, 네트워크 프린터 설정에 강하게 묶인다. 실제 현장에서도 프린터는 별도 PC나 로컬 네트워크에 붙어 있는 경우가 많다.

그래서 백엔드는 송장 데이터와 상태만 관리하고, 실제 출력은 나중에 `print-agent`가 처리하는 구조로 잡았다.

현재 상태 모델:

```
PENDING → PRINT_REQUESTED → PRINTED
                         ↘ FAILED
```

현재 구현은 PDF 렌더링까지는 하지 않는다. 대신 운송장 번호, 수령인 정보, 출력용 `labelData`, 출력 상태를 관리한다. 다음 단계에서 PDF 다운로드 API와 print-agent를 붙이면 된다.

---

## 14. DPS는 실제 장비 대신 시뮬레이터로 시작한다

DPS는 실제 장비와 TCP/IP 또는 장비별 프로토콜로 통신해야 한다. 개인 프로젝트에서 실제 장비까지 준비하기는 어렵다.

그래서 처음부터 실제 장비 연동을 목표로 잡기보다, 피킹 작업을 기반으로 점등 지시를 만들고 이를 시뮬레이터 화면이나 WebSocket으로 보여주는 방향이 더 현실적이라고 판단했다.

예상 흐름:

```
PickingTask 생성
→ DPSInstruction 생성
→ dps-agent 또는 WebSocket 시뮬레이터에 표시
→ 작업자 확인
→ CONFIRMED 처리
```

이렇게 해도 외부 장비 연동에서 중요한 상태 동기화, 실패 처리, 재시도 경계를 충분히 설명할 수 있다.

---

## 15. API 경계는 나중에 admin/pda로 나눈다

현재는 개발 속도를 위해 모든 API를 `/api/v1`에 두었다. 하지만 최종적으로는 운영자와 작업자의 API를 나누는 편이 맞다.

예상 구조:

```
/admin-api/v1/**  → 관리자 웹용
/pda-api/v1/**    → Android PDA 앱용
```

처음부터 나누면 DTO와 Controller가 많이 늘어나 MVP 속도가 떨어진다. 지금은 도메인 흐름을 먼저 구현하고, 화면을 붙이면서 필요한 응답 모델을 기준으로 API를 분리할 계획이다.

---

## 16. Docker Compose를 먼저 만든 이유

WMS는 DB와 Redis가 붙어야 제대로 테스트할 수 있다. 로컬 실행 방법이 복잡하면 프로젝트를 다시 켤 때마다 흐름이 끊긴다.

그래서 `infra/docker-compose.yml`로 PostgreSQL, Redis, 백엔드를 한 번에 띄우도록 했다.

```
cd infra
docker compose up --build
```

`ddl-auto=validate`를 쓰기 때문에 초기 스키마는 `infra/db/init.sql`에 명시했다. 테이블이 엔티티와 맞지 않으면 서버가 바로 실패하므로, 개발 중 스키마 불일치를 빨리 발견할 수 있다.

---

## 17. 예외는 공통 포맷으로 처리하되 도메인별 클래스로 나눈다

처음에는 `BusinessException`에 `ErrorCode`만 넣어 던지는 방식으로 시작했다. 기능이 적을 때는 빠르지만, 도메인이 늘어나면 서비스 코드에서 어떤 업무 예외인지 한눈에 보이지 않는다. 나중에 MSA로 나누게 되면 각 서비스가 자기 도메인의 예외를 소유하는 편이 더 자연스럽다.

그래서 구조를 아래처럼 바꿨다.

```
BusinessException
├── InboundOrderNotFoundException
├── OutboundOrderNotFoundException
├── ReturnOrderNotFoundException
├── PutawayTaskNotFoundException
├── PickingWaveNotFoundException
├── PickingTaskNotFoundException
├── ShippingLabelNotFoundException
└── InsufficientStockException
```

`ErrorCode`에는 코드, 메시지뿐 아니라 HTTP 상태도 같이 둔다. 전역 예외 핸들러는 예외 타입별로 응답 모양을 새로 만들지 않고, `ErrorCode`에 정의된 상태와 메시지를 그대로 사용한다.

이렇게 하면 컨트롤러 응답은 계속 같은 포맷을 유지하면서도, 도메인 내부 코드는 `throw new ShippingLabelNotFoundException()`처럼 읽히게 된다. 나중에 서비스를 분리하더라도 각 도메인 예외 클래스를 해당 서비스로 옮기기 쉽다.
