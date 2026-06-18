import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 50,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

export default function () {
  const warehouseId = Math.floor(Math.random() * 5) + 1;
  const url = `${BASE_URL}/api/v1/inventories?warehouseId=${warehouseId}&size=50`;

  const response = http.get(url, {
    tags: {
      api: 'inventory_query_by_warehouse',
      warehouseId: String(warehouseId),
    },
  });

  check(response, {
    'inventory query status is 200': (res) => res.status === 200,
    'inventory query has body': (res) => res.body && res.body.length > 0,
  });

  sleep(1);
}
