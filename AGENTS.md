# 창고지기 — Codex Agent 지침

## 프로젝트 개요
- 프로젝트명: 창고지기 (changgoji)
- 3PL 물류센터 WMS 백엔드 포트폴리오
- 기술 스택: Java 17, Spring Boot 3, PostgreSQL, JPA/QueryDSL, Redis, Docker

## 작업 규칙

### 파일 위치
- 모든 백엔드 코드는 `backend/` 폴더 안에 작성
- 설계 문서는 `docs/` 참고
- k6 테스트 스크립트는 `k6/` 폴더에 작성
- 루트나 다른 폴더에 코드 파일 생성 금지

### 패키지 구조
```
backend/src/main/java/com/warehouse/
├── common/
│   ├── exception/   (GlobalExceptionHandler, ErrorCode, BusinessException)
│   ├── response/    (ApiResponse<T>)
│   └── config/      (QueryDslConfig, RedisConfig, SwaggerConfig)
├── inbound/
│   ├── domain/
│   ├── repository/
│   ├── service/
│   └── controller/
├── inventory/
│   ├── domain/
│   ├── repository/
│   ├── service/
│   └── controller/
├── outbound/
│   ├── domain/
│   ├── repository/
│   ├── service/
│   └── controller/
└── returns/
    ├── domain/
    ├── repository/
    ├── service/
    └── controller/
```

---

## 코드 컨벤션 (반드시 준수)

### 엔티티

**Setter 절대 금지**
- @Setter 사용 금지
- 상태 변경은 반드시 의미 있는 도메인 메서드로 (confirm(), cancel(), allocate() 등)

**생성자 전략**
- @NoArgsConstructor(access = AccessLevel.PROTECTED) 필수
- @AllArgsConstructor 금지
- 생성은 반드시 정적 팩토리 메서드(create 또는 of)로

```java
// Good
public static InboundOrder create(Long warehouseId, Long supplierId, LocalDate scheduledDate) {
    InboundOrder order = new InboundOrder();
    order.warehouseId = warehouseId;
    order.supplierId = supplierId;
    order.scheduledDate = scheduledDate;
    order.status = InboundStatus.REQUESTED;
    return order;
}

// Bad — 절대 금지
@AllArgsConstructor
@Builder
public class InboundOrder { ... }
```

**@Builder 사용 기준**
- 엔티티: @Builder 금지. 정적 팩토리 메서드만 사용
- DTO (Request/Response): @Builder 허용. @AllArgsConstructor와 함께만 사용
- 필드 2개 이하 DTO는 생성자로 충분

**허용 어노테이션**
- 엔티티: @Getter, @NoArgsConstructor(access = PROTECTED)만
- @Data 금지 (equals/hashCode/toString 문제)

### DTO

**Request DTO — record 사용**
```java
public record InboundOrderCreateRequest(
    @NotNull Long warehouseId,
    @NotNull Long supplierId,
    @NotNull LocalDate scheduledDate,
    @NotEmpty List<InboundItemRequest> items
) {}
```

**Response DTO — from() 팩토리 메서드**
```java
public record InboundOrderResponse(Long id, String status, LocalDate scheduledDate) {
    public static InboundOrderResponse from(InboundOrder order) {
        return new InboundOrderResponse(
            order.getId(),
            order.getStatus().name(),
            order.getScheduledDate()
        );
    }
}
```

### 서비스

**트랜잭션**
- 클래스 레벨: @Transactional(readOnly = true)
- 쓰기 메서드만: @Transactional 개별 선언

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InboundService {

    @Transactional
    public InboundOrderResponse createInboundOrder(InboundOrderCreateRequest request) { ... }

    public InboundOrderResponse getInboundOrder(Long id) { ... }
}
```

**의존성 주입**
- @RequiredArgsConstructor + final 필드
- @Autowired 금지

### 예외 처리

**계층 구조**
```
BusinessException (RuntimeException)
├── InsufficientStockException
├── InvalidStatusException
├── InboundOrderNotFoundException
├── OutboundOrderNotFoundException
└── ReturnOrderNotFoundException
```

**ErrorCode Enum**
```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INSUFFICIENT_STOCK("INVENTORY_001", "재고가 부족합니다."),
    INVALID_STATUS("ORDER_001", "유효하지 않은 상태 전이입니다."),
    CONCURRENT_UPDATE("COMMON_001", "동시 요청으로 인해 처리에 실패했습니다. 다시 시도해주세요."),
    INVALID_INPUT("COMMON_002", "입력값이 올바르지 않습니다."),
    INBOUND_ORDER_NOT_FOUND("INBOUND_001", "입고 지시를 찾을 수 없습니다."),
    OUTBOUND_ORDER_NOT_FOUND("OUTBOUND_001", "출고 지시를 찾을 수 없습니다."),
    RETURN_ORDER_NOT_FOUND("RETURN_001", "반품 지시를 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
```

**GlobalExceptionHandler**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getErrorCode()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(OptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.fail(ErrorCode.CONCURRENT_UPDATE));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .findFirst()
            .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(ErrorCode.INVALID_INPUT, message));
    }
}
```

### 공통 응답
```java
@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }
}
```

### 패키지 의존 방향
```
controller → service → repository
                ↓
            domain (entity, exception)
```
- controller가 repository 직접 호출 금지
- service → service 호출 허용 (InboundService → InventoryService)
- entity가 repository/service 참조 금지

### 네이밍
| 구분 | 규칙 | 예시 |
|---|---|---|
| 엔티티 생성 | create() 또는 of() | InboundOrder.create(...) |
| 상태 변경 | 동사형 메서드 | confirm(), cancel(), allocate() |
| 조회 서비스 | get + 명사 | getInboundOrder() |
| 생성 서비스 | create + 명사 | createInboundOrder() |
| QueryDSL Repository | ~QueryRepository | InventoryQueryRepository |
