import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const INVENTORY_ID = __ENV.INVENTORY_ID || '1';
const PAGE_SIZE = Number(__ENV.PAGE_SIZE || '20');
const DEPTHS = [0, 100, 1000, 5000];

const offsetTrends = {
  0: new Trend('offset_page_0_duration', true),
  100: new Trend('offset_page_100_duration', true),
  1000: new Trend('offset_page_1000_duration', true),
  5000: new Trend('offset_page_5000_duration', true),
};

const cursorTrends = {
  0: new Trend('cursor_page_0_duration', true),
  100: new Trend('cursor_page_100_duration', true),
  1000: new Trend('cursor_page_1000_duration', true),
  5000: new Trend('cursor_page_5000_duration', true),
};

export const options = {
  scenarios: {
    offset_pagination: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      exec: 'offsetScenario',
    },
    cursor_pagination: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      exec: 'cursorScenario',
      startTime: '2s',
      maxDuration: '30m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
  },
};

export function offsetScenario() {
  for (const page of DEPTHS) {
    const url = `${BASE_URL}/api/v1/inventories/${INVENTORY_ID}/history/offset?page=${page}&size=${PAGE_SIZE}`;
    const response = http.get(url, {
      tags: {
        scenario: 'offset',
        pagination: 'offset',
        depth: `page_${page}`,
      },
    });

    offsetTrends[page].add(response.timings.duration);

    check(response, {
      'offset status is 200': (res) => res.status === 200,
      'offset response is success': (res) => isSuccess(res),
    });
  }
}

export function cursorScenario() {
  let cursor = null;
  const targetDepths = new Set(DEPTHS);

  for (let page = 0; page <= Math.max(...DEPTHS); page += 1) {
    const cursorQuery = cursor === null ? '' : `&cursor=${cursor}`;
    const url = `${BASE_URL}/api/v1/inventories/${INVENTORY_ID}/history/cursor?size=${PAGE_SIZE}${cursorQuery}`;
    const response = http.get(url, {
      tags: {
        scenario: 'cursor',
        pagination: 'cursor',
        depth: targetDepths.has(page) ? `page_${page}` : 'warmup',
      },
    });

    check(response, {
      'cursor status is 200': (res) => res.status === 200,
      'cursor response is success': (res) => isSuccess(res),
    });

    if (targetDepths.has(page)) {
      cursorTrends[page].add(response.timings.duration);
    }

    const histories = responseData(response);
    if (histories.length === 0) {
      break;
    }

    cursor = histories[histories.length - 1].id;
  }
}

function isSuccess(response) {
  try {
    const body = response.json();
    return response.status === 200 && body.success === true && Array.isArray(body.data);
  } catch (e) {
    return false;
  }
}

function responseData(response) {
  try {
    const body = response.json();
    return Array.isArray(body.data) ? body.data : [];
  } catch (e) {
    return [];
  }
}

export function handleSummary(data) {
  return {
    stdout: `${summaryTable(data)}\n`,
  };
}

function summaryTable(data) {
  const lines = [
    '',
    'OFFSET vs Cursor pagination p95 summary',
    '',
    '| Depth | OFFSET p95 | Cursor p95 |',
    '|---:|---:|---:|',
  ];

  for (const depth of DEPTHS) {
    lines.push(
      `| ${depth} | ${p95(data, `offset_page_${depth}_duration`)} | ${p95(data, `cursor_page_${depth}_duration`)} |`
    );
  }

  lines.push('');
  lines.push(`failed requests: ${rate(data, 'http_req_failed')}`);
  lines.push('');

  return lines.join('\n');
}

function p95(data, metricName) {
  const metric = data.metrics[metricName];
  if (!metric || !metric.values || metric.values['p(95)'] === undefined) {
    return 'N/A';
  }
  return `${metric.values['p(95)'].toFixed(2)} ms`;
}

function rate(data, metricName) {
  const metric = data.metrics[metricName];
  if (!metric || !metric.values || metric.values.rate === undefined) {
    return 'N/A';
  }
  return `${(metric.values.rate * 100).toFixed(2)}%`;
}
