# 시스템 아키텍처

## 프로젝트 개요

**창고지기**는 3PL 물류센터의 입고·재고·출고·반품 흐름을 관리하는 WMS(Warehouse Management System) 백엔드입니다.

> WMS란 창고 내 재고의 입고부터 출고까지 전 과정을 추적하고 관리하는 시스템으로,
> 카카오스타일·무신사·쿠팡 같은 이커머스 풀필먼트 센터에서 핵심 인프라로 사용됩니다.

---

## 도메인 구성

| 도메인 | 설명 | 구현 여부 |
|---|---|---|
| 입고 (Inbound) | 입고 지시 등록 → 실사 → 확정 → 재고 반영 | ✅ 구현 |
| 재고 (Inventory) | 재고 조회, 차감, 복구, 이력 관리 | ✅ 구현 |
| 출고 (Outbound) | 출고 지시 → 재고 할당 → 출고 확정 | ✅ 구현 |
| 반품 (Returns) | 반품 접수 → 실사 → 재고 복구 | ✅ 구현 |
| 적치 (Putaway) | 입고 후 위치 지정 | 설계만 |
| 피킹 (Picking) | 출고 지시 기반 피킹 웨이브 | 설계만 |
| DPS | 장비 프로토콜 제어 | 설계만 |
| 송장 출력 | PDF 렌더링 + 프린터 전송 | 설계만 |

---

## 실행 단위

```
changgoji/
├── backend/          Spring Boot API 서버 (구현)
├── docs/             설계 문서
├── k6/               부하테스트 스크립트
└── README.md
```

향후 확장 계획 (설계만 존재)
- `admin-web/` — 관리자 화면 (React)
- `pda-app/` — PDA 단말 연동
- `dps-agent/` — DPS 장비 Ethernet/TCP 제어
- `print-agent/` — 송장 출력 Windows 에이전트

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| DB | PostgreSQL |
| ORM | Spring Data JPA + QueryDSL 5 |
| Cache | Redis (Lettuce) |
| 문서화 | SpringDoc OpenAPI 3 (Swagger UI) |
| 테스트 | JUnit 5, k6 (부하테스트) |
| 컨테이너 | Docker, Docker Compose |

---

## 레이어 구조

```
Controller
    ↓  (Request DTO)
Service
    ↓  (Domain Method 호출)
Repository / QueryRepository
    ↓
Domain (Entity, Enum, Exception)
```

**의존 방향 규칙**
- Controller → Service → Repository 단방향
- Controller가 Repository 직접 호출 금지
- Service 간 호출 허용 (InboundService → InventoryService)
- Entity가 Service/Repository 참조 금지

---

## 도메인 흐름 전체

```
[입고 지시] → [실사 입고] → [입고 확정]
                                  ↓
                            [재고 증가]
                                  ↓
                  [출고 지시] → [재고 할당] → [출고 확정] → [재고 차감 확정]
                                                   ↓
                                             [반품 접수] → [반품 실사]
                                                               ↓
                                                    RESELLABLE → [재고 복구]
                                                    DEFECTIVE  → [불량 이력]
```

---

## 패키지 구조 (backend/)

```
com.warehouse
├── common
│   ├── exception
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── (도메인별 Exception)
│   ├── response
│   │   └── ApiResponse.java
│   └── config
│       ├── QueryDslConfig.java
│       ├── RedisConfig.java
│       └── SwaggerConfig.java
├── inbound
│   ├── domain
│   │   ├── InboundOrder.java
│   │   ├── InboundItem.java
│   │   ├── InboundReceipt.java
│   │   └── InboundStatus.java (Enum)
│   ├── repository
│   ├── service
│   │   └── InboundService.java
│   └── controller
│       └── InboundController.java
├── inventory
│   ├── domain
│   │   ├── Inventory.java
│   │   ├── InventoryLocation.java
│   │   ├── InventoryHistory.java
│   │   └── ChangeType.java (Enum)
│   ├── repository
│   │   └── InventoryQueryRepository.java
│   ├── service
│   │   └── InventoryService.java
│   └── controller
│       └── InventoryController.java
├── outbound
│   ├── domain
│   │   ├── OutboundOrder.java
│   │   ├── OutboundItem.java
│   │   └── OutboundStatus.java (Enum)
│   ├── repository
│   ├── service
│   │   └── OutboundService.java
│   └── controller
│       └── OutboundController.java
└── returns
    ├── domain
    │   ├── ReturnOrder.java
    │   ├── ReturnItem.java
    │   ├── ReturnStatus.java (Enum)
    │   └── ItemCondition.java (Enum)
    ├── repository
    ├── service
    │   └── ReturnService.java
    └── controller
        └── ReturnController.java
```
