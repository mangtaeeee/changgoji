# 시스템 아키텍처

## 프로젝트 개요

**창고지기**는 3PL 물류센터의 입고, 재고, 출고, 반품 흐름을 관리하는 WMS(Warehouse Management System) 백엔드 프로젝트다.

이 프로젝트는 배송&물류팀 JD를 보고 시작했다. 단순 CRUD가 아니라, 이커머스 물류에서 실제로 문제가 되는 재고 정확도, 작업 상태 추적, 현장 작업 흐름, 외부 장비/출력 연동 경계를 코드로 보여주는 것이 목표다.

---

## 현재 구현 범위

| 도메인 | 설명 | 구현 여부 |
|---|---|---|
| 입고 (Inbound) | 입고 지시 등록 → 실사 → 확정 → 재고 반영 | ✅ 구현 |
| 재고 (Inventory) | 재고 수량, 위치 재고, 재고 이력 관리 | ✅ 구현 |
| 출고 (Outbound) | 출고 지시 → 재고 할당 → 출고 확정/취소 | ✅ 구현 |
| 반품 (Returns) | 반품 접수 → 실사 → 재고 복구/불량 처리 | ✅ 구현 |
| 적치 (Putaway) | 입고 확정 후 위치 적치 작업 생성/확정 | ✅ 구현 |
| 피킹 (Picking) | 출고 할당 후 피킹 웨이브/작업 생성/완료 | ✅ 구현 |
| 송장 출력 (Shipping Label) | 송장 생성, 출력 요청, 출력 결과 상태 관리 | ✅ 구현 |
| DPS | 피킹 작업 기반 점등 지시, 장비 시뮬레이터 | 설계만 |

---

## 실행 단위

```
changgoji/
├── backend/          Spring Boot API 서버
├── infra/            Docker Compose, PostgreSQL/Redis 초기 설정
├── docs/             설계 문서
├── k6/               부하테스트 스크립트
└── README.md
```

확장 예정 실행 단위:
- `admin-web/` — 운영자 웹. 지시 생성, 재고 조회, 작업 현황 모니터링
- `android-app/` — 작업자 PDA 앱. 입고 실사, 적치, 피킹, 반품 검수
- `dps-agent/` — DPS 장비 또는 시뮬레이터 연동
- `print-agent/` — 송장 출력 에이전트

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
| 컨테이너 | Docker, Docker Compose |
| 테스트 계획 | JUnit 5, k6 |

---

## 레이어 구조

```
Controller
    ↓ Request/Response DTO
Service
    ↓ Domain Method 호출
Repository / QueryRepository
    ↓
Domain Entity
```

규칙:
- Controller는 Service만 호출한다.
- Service는 Repository와 다른 도메인 Service를 호출할 수 있다.
- Entity는 Repository나 Service를 참조하지 않는다.
- 상태 변경은 Setter가 아니라 도메인 메서드로 처리한다.

---

## 전체 업무 흐름

```
[입고 지시]
    → [실사 입고]
    → [입고 확정]
    → [재고 증가]
    → [적치 작업 생성]
    → [위치 적치 확정]

[출고 지시]
    → [재고 할당]
    → [피킹 웨이브/작업 생성]
    → [피킹 완료]
    → [송장 생성/출력 요청]
    → [출고 확정]

[반품 접수]
    → [반품 실사]
    → RESELLABLE: 재고 복구
    → DEFECTIVE: 불량 이력 기록
```

---

## 패키지 구조

```
com.warehouse
├── common
│   ├── config
│   ├── exception
│   └── response
├── inbound
│   ├── controller
│   ├── domain
│   ├── repository
│   └── service
├── inventory
│   ├── controller
│   ├── domain
│   ├── repository
│   └── service
├── outbound
│   ├── controller
│   ├── domain
│   ├── repository
│   └── service
├── returns
│   ├── controller
│   ├── domain
│   ├── repository
│   └── service
├── putaway
│   ├── controller
│   ├── domain
│   ├── repository
│   └── service
├── picking
│   ├── controller
│   ├── domain
│   ├── repository
│   └── service
└── shipping
    ├── controller
    ├── domain
    ├── repository
    └── service
```

---

## 구현상 주의점

- `ddl-auto=validate`를 사용하므로 Docker 초기 실행 시 `infra/db/init.sql`의 테이블 정의가 엔티티와 맞아야 한다.
- 현재 API는 모두 `/api/v1` 아래에 있다. 화면 개발 단계에서 관리자용 API와 PDA용 API를 분리할 계획이다.
- 송장은 현재 PDF 파일 생성까지는 하지 않고, 출력용 데이터와 출력 상태를 관리한다.
- DPS는 아직 구현 전이다. 실제 장비 없이도 시연할 수 있도록 시뮬레이터 방식으로 구현할 계획이다.
