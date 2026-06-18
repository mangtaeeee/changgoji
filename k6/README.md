# k6 부하 테스트

Spring Boot 서버를 실행한 뒤 아래 명령으로 테스트한다.

```powershell
k6 run --env BASE_URL=http://localhost:8080 k6/inventory-query-by-warehouse.js
k6 run --env BASE_URL=http://localhost:8080 --env INVENTORY_ID=1 k6/history-pagination-compare.js
```

## inventory-query-by-warehouse.js

창고 단위 재고 조회 API의 응답시간을 측정한다.

```http
GET /api/v1/inventories?warehouseId={1~5}&page=0&size=50
```

- VU 50
- 30초 동안 실행
- warehouseId는 1~5 중 랜덤 선택
- page는 0, size는 50으로 고정
- 페이지네이션 적용 전/후의 `http_req_duration` p95, p99를 비교한다.

페이지네이션 적용 후 재측정 결과:

| 항목 | 적용 전 | 적용 후 |
|---|---:|---:|
| p95 | 7.74s | 75.72ms |
| p99 | 10.04s | 132.22ms |
| data_received | 1.0GB / 30s | 11MB / 30s |
| threshold | FAIL | PASS |

## history-pagination-compare.js

재고 이력 조회 API의 OFFSET 방식과 Cursor 방식 응답시간을 비교한다.

```http
GET /api/v1/inventories/{id}/history/offset?page={page}&size=20
GET /api/v1/inventories/{id}/history/cursor?cursor={lastId}&size=20
```

- OFFSET은 page 0, 100, 1000, 5000을 직접 조회한다.
- Cursor는 첫 페이지부터 순차 조회하면서 0, 100, 1000, 5000번째 페이지에 도달했을 때의 응답시간을 기록한다.
- summary에 페이지 깊이별 p95 응답시간 표를 출력한다.

Cursor 방식은 이전 페이지의 마지막 ID가 있어야 다음 페이지를 조회할 수 있다. 그래서 5000번째 페이지까지 측정하려면 실제로 5000번 이상 순차 요청을 수행한다.

## 결과 해석

- `http_req_duration p(95)`: 상위 95% 요청이 이 시간 안에 끝났는지 확인한다.
- `http_req_duration p(99)`: 느린 요청의 꼬리가 얼마나 긴지 확인한다.
- `http_req_failed`: 실패율이 1% 이상이면 먼저 오류 원인을 확인한다.
- OFFSET은 page가 깊어질수록 앞 데이터를 건너뛰는 비용이 커지는지 본다.
- Cursor는 깊은 페이지로 이동해도 응답시간이 안정적인지 본다.

주의할 점은 현재 기본 시딩 데이터가 inventory 1건당 평균 5개의 이력만 만든다는 것이다. OFFSET과 Cursor 차이를 크게 보려면 특정 `INVENTORY_ID`에 충분히 많은 `inventory_history` 데이터를 추가로 넣고 테스트해야 한다.

## 로컬 측정 결과

측정 환경:

- Spring Boot: `http://localhost:18080`, `http://localhost:18081`
- PostgreSQL: Docker 로컬 컨테이너
- inventory: 100,000건
- inventory_history: 620,000건
- pagination 비교 대상: `inventory_id=1`, 이력 120,005건

### 창고 단위 재고 조회 — 페이지네이션 적용 전후

페이지네이션 적용 전:

![inventory-query-by-warehouse-before](results/inventory-query-by-warehouse-18080.png)

페이지네이션 적용 후:

![inventory-query-by-warehouse-after](results/inventory-query-by-warehouse-page-size-50.png)

| 항목 | 페이지네이션 전 | 페이지네이션 후 |
|---|---:|---:|
| http_req_duration p95 | 7.74s | 75.72ms |
| http_req_duration p99 | 10.04s | 132.22ms |
| data_received | 1.0GB / 30s | 11MB / 30s |
| threshold | FAIL | PASS |

인덱스는 정상적으로 적용됐지만, 페이지네이션 전에는 창고 하나의 재고 약 20,000건을 한 번에 응답하면서 JSON 직렬화와 네트워크 전송 비용이 커졌다. `size=50` 페이지네이션 적용 후 threshold를 통과했다.

### OFFSET vs Cursor 페이지네이션

![history-pagination-compare](results/history-pagination-compare-18080.png)

| Depth | OFFSET p95 | Cursor p95 |
|---:|---:|---:|
| 0 | 12.16ms | 7.95ms |
| 100 | 7.77ms | 5.68ms |
| 1000 | 9.28ms | 4.15ms |
| 5000 | 20.34ms | 4.25ms |

깊은 페이지로 갈수록 OFFSET은 앞 데이터를 건너뛰는 비용이 커졌고, 5000 페이지에서 p95가 20.34ms까지 증가했다. Cursor 방식은 마지막 ID 기준으로 다음 범위를 조회하므로 깊이가 깊어져도 4~8ms 수준으로 비교적 안정적이었다.
