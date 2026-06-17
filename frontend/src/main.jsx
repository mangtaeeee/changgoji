import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  ArrowRight,
  Boxes,
  ClipboardCheck,
  ClipboardList,
  FileText,
  PackageCheck,
  RefreshCw,
  RotateCcw,
  Send,
  Truck,
  Warehouse,
} from 'lucide-react';
import './styles.css';

const API_BASE = '/api/v1';

async function api(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });
  const body = await response.json().catch(() => null);
  if (!response.ok || body?.success === false) {
    throw new Error(body?.message || `HTTP ${response.status}`);
  }
  return body?.data;
}

function todayPlus(days) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString().slice(0, 10);
}

const initialForms = {
  inbound: {
    warehouseId: '1',
    supplierId: '10',
    scheduledDate: todayPlus(3),
    skuId: '100',
    skuName: '상품A',
    orderedQty: '50',
    inboundOrderId: '1',
    inboundItemId: '1',
    receivedQty: '48',
    confirmedBy: '999',
  },
  inventory: {
    warehouseId: '1',
    skuId: '100',
    inventoryId: '1',
    adjustQty: '-1',
    reason: '실사 조정',
  },
  putaway: {
    warehouseId: '1',
    putawayTaskId: '1',
    assignedTo: '999',
    confirmedLocation: 'A-01-03',
  },
  outbound: {
    warehouseId: '1',
    orderId: 'ORDER-20260617-001',
    skuId: '100',
    requestedQty: '5',
    locationCode: 'A-01-03',
    outboundOrderId: '1',
  },
  picking: {
    warehouseId: '1',
    pickingWaveId: '1',
    pickingTaskId: '1',
    assignedTo: '999',
  },
  shipping: {
    outboundOrderId: '1',
    shippingLabelId: '1',
    carrier: 'CJ대한통운',
    receiverName: '홍길동',
    receiverPhone: '010-1234-5678',
    receiverAddress: '서울시 강남구 테헤란로 123',
    failureReason: '프린터 연결 실패',
  },
  returns: {
    outboundOrderId: '1',
    warehouseId: '1',
    skuId: '100',
    skuName: '상품A',
    requestedQty: '2',
    returnOrderId: '1',
    returnItemId: '1',
    receivedQty: '2',
    condition: 'RESELLABLE',
  },
};

function getInitialForms() {
  const params = new URLSearchParams(window.location.search);
  const forms = JSON.parse(JSON.stringify(initialForms));
  const sharedFields = ['warehouseId', 'skuId', 'skuName', 'outboundOrderId'];

  sharedFields.forEach((field) => {
    const value = params.get(field);
    if (!value) return;
    Object.keys(forms).forEach((section) => {
      if (field in forms[section]) {
        forms[section][field] = value;
      }
    });
  });

  Object.keys(forms).forEach((section) => {
    Object.keys(forms[section]).forEach((field) => {
      const value = params.get(field);
      if (value) {
        forms[section][field] = value;
      }
    });
  });

  return forms;
}

function getInitialInventoryRows() {
  const params = new URLSearchParams(window.location.search);
  const skuId = params.get('skuId');
  if (!skuId || !params.get('availableQty')) {
    return [];
  }
  return [{
    id: Number(params.get('inventoryId') || 0),
    warehouseId: Number(params.get('warehouseId') || 1),
    skuId: Number(skuId),
    skuName: params.get('skuName') || '-',
    availableQty: Number(params.get('availableQty') || 0),
    allocatedQty: Number(params.get('allocatedQty') || 0),
    locationCode: params.get('locationCode') || '-',
    locationQty: Number(params.get('locationQty') || 0),
  }];
}

function getInitialDemoSummary() {
  const params = new URLSearchParams(window.location.search);
  if (!params.get('skuId') || !params.get('availableQty')) {
    return null;
  }
  return {
    skuId: Number(params.get('skuId')),
    skuName: params.get('skuName') || '-',
    inboundOrderId: Number(params.get('inboundOrderId') || 0),
    outboundOrderId: Number(params.get('outboundOrderId') || 0),
    shippingLabelId: Number(params.get('shippingLabelId') || 0),
    returnOrderId: Number(params.get('returnOrderId') || 0),
    availableQty: Number(params.get('availableQty') || 0),
    allocatedQty: Number(params.get('allocatedQty') || 0),
  };
}

const tabs = [
  { id: 'dashboard', label: '대시보드', icon: Warehouse },
  { id: 'inbound', label: '입고', icon: ClipboardList },
  { id: 'inventory', label: '재고', icon: Boxes },
  { id: 'putaway', label: '적치', icon: PackageCheck },
  { id: 'outbound', label: '출고', icon: Truck },
  { id: 'picking', label: '피킹', icon: ClipboardCheck },
  { id: 'shipping', label: '송장', icon: FileText },
  { id: 'returns', label: '반품', icon: RotateCcw },
];

const warehouseOptions = [
  { label: '1센터 / 기본 창고', value: '1' },
  { label: '2센터 / 예비 창고', value: '2' },
];

const supplierOptions = [
  { label: '동대문 공급사', value: '10' },
  { label: '브랜드 공급사', value: '20' },
];

const workerOptions = [
  { label: '김작업 매니저', value: '999' },
  { label: '박검수 작업자', value: '1001' },
];

function getInitialTab() {
  const view = new URLSearchParams(window.location.search).get('view');
  return tabs.some((tab) => tab.id === view) ? view : 'dashboard';
}

function App() {
  const [activeTab, setActiveTab] = useState(getInitialTab);
  const [forms, setForms] = useState(getInitialForms);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [inventoryRows, setInventoryRows] = useState(getInitialInventoryRows);
  const [putawayRows, setPutawayRows] = useState([]);
  const [pickingRows, setPickingRows] = useState([]);
  const [demoSummary, setDemoSummary] = useState(getInitialDemoSummary);
  const [toast, setToast] = useState('백엔드가 켜져 있으면 새로고침으로 현황을 불러옵니다.');

  const updateForm = (section, field, value) => {
    setForms((prev) => ({
      ...prev,
      [section]: { ...prev[section], [field]: value },
    }));
  };

  const updateSection = (section, values) => {
    setForms((prev) => ({
      ...prev,
      [section]: { ...prev[section], ...values },
    }));
  };

  const changeTab = (tabId) => {
    const params = new URLSearchParams(window.location.search);
    if (tabId === 'dashboard') {
      params.delete('view');
    } else {
      params.set('view', tabId);
    }
    const query = params.toString();
    window.history.replaceState(null, '', query ? `?${query}` : window.location.pathname);
    setActiveTab(tabId);
  };

  const run = async (label, fn) => {
    setLoading(true);
    setToast(`${label} 처리 중`);
    try {
      const data = await fn();
      setResult({ label, data });
      setToast(`${label} 완료`);
      return data;
    } catch (error) {
      setResult({ label, error: error.message });
      setToast(error.message);
      return null;
    } finally {
      setLoading(false);
    }
  };

  const refreshDashboard = async () => {
    const warehouseId = forms.inventory.warehouseId;
    const [inventories, putaways, waves] = await Promise.allSettled([
      api(`/inventories?warehouseId=${warehouseId}`),
      api(`/putaway-tasks?warehouseId=${warehouseId}&status=PENDING`),
      api(`/picking-waves?warehouseId=${warehouseId}&status=OPEN`),
    ]);
    setInventoryRows(inventories.status === 'fulfilled' ? inventories.value : []);
    setPutawayRows(putaways.status === 'fulfilled' ? putaways.value : []);
    setPickingRows(waves.status === 'fulfilled' ? waves.value : []);
  };

  const runDemoScenario = () => run('전체 시연 데이터 생성', async () => {
    const seed = Date.now().toString().slice(-6);
    const skuId = Number(`8${seed}`);
    const skuName = `데모상품-${seed}`;
    const warehouseId = 1;
    const receivedQty = 48;
    const outboundQty = 5;
    const returnQty = 2;

    const inbound = await api('/inbound-orders', {
      method: 'POST',
      body: JSON.stringify({
        warehouseId,
        supplierId: 10,
        scheduledDate: todayPlus(3),
        items: [{ skuId, skuName, orderedQty: 50 }],
      }),
    });
    const inboundItemId = inbound.items[0].id;
    await api(`/inbound-orders/${inbound.id}/receive`, {
      method: 'PATCH',
      body: JSON.stringify({ items: [{ inboundItemId, receivedQty }] }),
    });
    await api(`/inbound-orders/${inbound.id}/confirm`, {
      method: 'PATCH',
      body: JSON.stringify({ confirmedBy: 999, memo: '대시보드 전체 시연' }),
    });

    const putaways = await api(`/putaway-tasks?warehouseId=${warehouseId}&status=PENDING`);
    const putaway = putaways.find((task) => task.skuId === skuId) || putaways[0];
    await api(`/putaway-tasks/${putaway.id}/start`, {
      method: 'PATCH',
      body: JSON.stringify({ assignedTo: 999 }),
    });
    await api(`/putaway-tasks/${putaway.id}/confirm`, {
      method: 'PATCH',
      body: JSON.stringify({ confirmedLocation: 'A-01-03' }),
    });

    const outbound = await api('/outbound-orders', {
      method: 'POST',
      body: JSON.stringify({
        warehouseId,
        orderId: `DEMO-${seed}`,
        items: [{ skuId, requestedQty: outboundQty, locationCode: 'A-01-03' }],
      }),
    });
    await api(`/outbound-orders/${outbound.id}/allocate`, { method: 'PATCH' });

    const waves = await api(`/picking-waves?warehouseId=${warehouseId}&status=OPEN`);
    const wave = waves.find((item) => item.outboundOrderId === outbound.id) || waves[0];
    const tasks = await api(`/picking-waves/${wave.id}/tasks`);
    await api(`/picking-tasks/${tasks[0].id}/pick`, {
      method: 'PATCH',
      body: JSON.stringify({ assignedTo: 999 }),
    });

    const label = await api('/shipping-labels', {
      method: 'POST',
      body: JSON.stringify({
        outboundOrderId: outbound.id,
        carrier: 'CJ대한통운',
        receiverName: '홍길동',
        receiverPhone: '010-1234-5678',
        receiverAddress: '서울시 강남구 테헤란로 123',
      }),
    });
    await api(`/shipping-labels/${label.id}/print`, { method: 'POST' });
    await api(`/shipping-labels/${label.id}/printed`, { method: 'PATCH' });
    await api(`/outbound-orders/${outbound.id}/ship`, { method: 'PATCH' });

    const returnOrder = await api('/return-orders', {
      method: 'POST',
      body: JSON.stringify({
        outboundOrderId: outbound.id,
        warehouseId,
        reason: 'CUSTOMER_CHANGE',
        items: [{ skuId, skuName, requestedQty: returnQty }],
      }),
    });
    await api(`/return-orders/${returnOrder.id}/receive`, {
      method: 'PATCH',
      body: JSON.stringify({
        items: [{ returnItemId: returnOrder.items[0].id, receivedQty: returnQty, condition: 'RESELLABLE' }],
      }),
    });
    await api(`/return-orders/${returnOrder.id}/complete`, { method: 'PATCH' });

    const inventories = await api(`/inventories?warehouseId=${warehouseId}`);
    const pendingPutaways = await api(`/putaway-tasks?warehouseId=${warehouseId}&status=PENDING`);
    const openWaves = await api(`/picking-waves?warehouseId=${warehouseId}&status=OPEN`);
    const demoInventory = inventories.find((item) => item.skuId === skuId);

    setInventoryRows(inventories);
    setPutawayRows(pendingPutaways);
    setPickingRows(openWaves);
    updateSection('inbound', {
      warehouseId: String(warehouseId),
      skuId: String(skuId),
      skuName,
      inboundOrderId: String(inbound.id),
      inboundItemId: String(inboundItemId),
      receivedQty: String(receivedQty),
    });
    updateSection('inventory', {
      warehouseId: String(warehouseId),
      skuId: String(skuId),
      inventoryId: String(demoInventory?.id || ''),
    });
    updateSection('putaway', {
      warehouseId: String(warehouseId),
      putawayTaskId: String(putaway.id),
      confirmedLocation: 'A-01-03',
    });
    updateSection('outbound', {
      warehouseId: String(warehouseId),
      orderId: `DEMO-${seed}`,
      skuId: String(skuId),
      outboundOrderId: String(outbound.id),
    });
    updateSection('picking', {
      warehouseId: String(warehouseId),
      pickingWaveId: String(wave.id),
      pickingTaskId: String(tasks[0].id),
    });
    updateSection('shipping', {
      outboundOrderId: String(outbound.id),
      shippingLabelId: String(label.id),
    });
    updateSection('returns', {
      outboundOrderId: String(outbound.id),
      warehouseId: String(warehouseId),
      skuId: String(skuId),
      skuName,
      returnOrderId: String(returnOrder.id),
      returnItemId: String(returnOrder.items[0].id),
      receivedQty: String(returnQty),
    });
    setDemoSummary({
      skuId,
      skuName,
      inboundOrderId: inbound.id,
      outboundOrderId: outbound.id,
      shippingLabelId: label.id,
      returnOrderId: returnOrder.id,
      availableQty: demoInventory?.availableQty,
      allocatedQty: demoInventory?.allocatedQty,
    });

    return {
      skuId,
      skuName,
      inboundOrderId: inbound.id,
      putawayTaskId: putaway.id,
      outboundOrderId: outbound.id,
      pickingWaveId: wave.id,
      pickingTaskId: tasks[0].id,
      shippingLabelId: label.id,
      returnOrderId: returnOrder.id,
      inventory: demoInventory,
    };
  });

  useEffect(() => {
    refreshDashboard().catch(() => undefined);
  }, []);

  const metrics = useMemo(() => {
    const available = inventoryRows.reduce((sum, row) => sum + (row.availableQty || 0), 0);
    const allocated = inventoryRows.reduce((sum, row) => sum + (row.allocatedQty || 0), 0);
    const locationQty = inventoryRows.reduce((sum, row) => sum + (row.locationQty || 0), 0);
    return { available, allocated, locationQty };
  }, [inventoryRows]);

  const views = {
    dashboard: (
      <Dashboard
        metrics={metrics}
        inventoryRows={inventoryRows}
        putawayRows={putawayRows}
        pickingRows={pickingRows}
        demoSummary={demoSummary}
        onRefresh={() => run('대시보드 새로고침', refreshDashboard)}
        onRunDemo={runDemoScenario}
      />
    ),
    inbound: <InboundView forms={forms} updateForm={updateForm} updateSection={updateSection} run={run} changeTab={changeTab} refreshDashboard={refreshDashboard} />,
    inventory: <InventoryView forms={forms} updateForm={updateForm} run={run} setInventoryRows={setInventoryRows} changeTab={changeTab} />,
    putaway: <PutawayView forms={forms} updateForm={updateForm} run={run} setPutawayRows={setPutawayRows} changeTab={changeTab} refreshDashboard={refreshDashboard} />,
    outbound: <OutboundView forms={forms} updateForm={updateForm} updateSection={updateSection} run={run} changeTab={changeTab} />,
    picking: <PickingView forms={forms} updateForm={updateForm} run={run} setPickingRows={setPickingRows} changeTab={changeTab} />,
    shipping: <ShippingView forms={forms} updateForm={updateForm} run={run} changeTab={changeTab} />,
    returns: <ReturnsView forms={forms} updateForm={updateForm} updateSection={updateSection} run={run} changeTab={changeTab} />,
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">창</div>
          <div>
            <strong>창고지기</strong>
            <span>WMS Console</span>
          </div>
        </div>
        <nav className="nav-list" aria-label="주요 메뉴">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                className={activeTab === tab.id ? 'nav-item active' : 'nav-item'}
                onClick={() => changeTab(tab.id)}
                title={tab.label}
              >
                <Icon size={18} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>
      </aside>

      <main className="main">
        <header className="topbar">
          <div>
            <p className="eyebrow">3PL Warehouse Management</p>
            <h1>{tabs.find((tab) => tab.id === activeTab)?.label}</h1>
          </div>
          <button className="icon-button" onClick={() => run('대시보드 새로고침', refreshDashboard)} title="새로고침">
            <RefreshCw size={18} />
          </button>
        </header>

        <div className="toast">{loading ? '처리 중...' : toast}</div>
        {activeTab !== 'dashboard' && <WorkGuide activeTab={activeTab} />}

        <section className="workspace">
          <div className="content-panel">{views[activeTab]}</div>
          <ResultPanel result={result} />
        </section>
      </main>
    </div>
  );
}

function Dashboard({ metrics, inventoryRows, putawayRows, pickingRows, demoSummary, onRefresh, onRunDemo }) {
  return (
    <div className="stack">
      <div className="toolbar">
        <h2>운영 현황</h2>
        <div className="toolbar-actions">
          <button className="primary-button" onClick={onRunDemo}>
            <ArrowRight size={16} />
            시연 데이터 만들기
          </button>
          <button className="secondary-button" onClick={onRefresh}>
            <RefreshCw size={16} />
            새로고침
          </button>
        </div>
      </div>
      <div className="demo-banner">
        <div>
          <strong>버튼 한 번으로 입고부터 반품까지</strong>
          <p>시연 데이터 만들기를 누르면 입고 확정, 적치, 출고 할당, 피킹, 송장 출력, 반품 재고 복구가 실제 API로 실행됩니다.</p>
        </div>
        {demoSummary && (
          <div className="demo-summary">
            <span>SKU {demoSummary.skuId}</span>
            <span>가용 {formatCell(demoSummary.availableQty)}</span>
            <span>할당 {formatCell(demoSummary.allocatedQty)}</span>
            <span>반품 #{demoSummary.returnOrderId}</span>
          </div>
        )}
      </div>
      <div className="flow-guide">
        {[
          ['1', '입고 생성', '입고 지시를 만들고 실사 수량을 확정합니다.'],
          ['2', '적치 확정', '확정된 입고 수량을 실제 로케이션에 넣습니다.'],
          ['3', '출고 할당', '가용 재고를 할당 재고로 이동시킵니다.'],
          ['4', '피킹/송장', '현장 피킹 후 송장을 출력 요청합니다.'],
          ['5', '반품 처리', '재판매 가능 여부에 따라 재고를 복구합니다.'],
        ].map(([step, title, text]) => (
          <div className="flow-step" key={step}>
            <span>{step}</span>
            <strong>{title}</strong>
            <p>{text}</p>
          </div>
        ))}
      </div>
      <div className="metric-grid">
        <Metric label="가용 재고" value={metrics.available} />
        <Metric label="할당 재고" value={metrics.allocated} />
        <Metric label="위치 재고" value={metrics.locationQty} />
        <Metric label="대기 적치" value={putawayRows.length} />
      </div>
      <DataTable
        title="재고 목록"
        rows={inventoryRows}
        columns={[
          ['skuId', 'SKU'],
          ['skuName', '상품명'],
          ['availableQty', '가용'],
          ['allocatedQty', '할당'],
          ['locationCode', '위치'],
          ['locationQty', '위치수량'],
        ]}
      />
      <DataTable
        title="대기 작업"
        rows={[...putawayRows, ...pickingRows]}
        columns={[
          ['id', 'ID'],
          ['status', '상태'],
          ['warehouseId', '창고'],
          ['skuId', 'SKU'],
          ['qty', '수량'],
        ]}
      />
    </div>
  );
}

function Metric({ label, value }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function WorkGuide({ activeTab }) {
  const guides = {
    inbound: ['입고부터 시작합니다.', '상품이 창고에 들어왔다고 가정하고 입고를 완료하면 재고가 늘어납니다.'],
    inventory: ['재고를 확인합니다.', '입고 확정 후 가용 재고가 늘었는지 확인합니다.'],
    putaway: ['상품 위치를 확정합니다.', '입고된 상품을 실제 로케이션에 넣어 위치 재고를 만듭니다.'],
    outbound: ['출고 요청을 처리합니다.', '주문이 들어오면 재고를 먼저 할당하고 피킹 작업을 만듭니다.'],
    picking: ['현장 피킹을 완료합니다.', '출고할 상품을 작업자가 집었다고 기록합니다.'],
    shipping: ['송장을 출력합니다.', '배송에 필요한 송장을 만들고 출력 상태를 변경합니다.'],
    returns: ['반품을 검수합니다.', '재판매 가능한 상품이면 다시 재고로 복구합니다.'],
  };
  const guide = guides[activeTab];
  if (!guide) return null;
  return (
    <div className="work-guide">
      <strong>{guide[0]}</strong>
      <span>{guide[1]}</span>
    </div>
  );
}

function InboundView({ forms, updateForm, updateSection, run, changeTab, refreshDashboard }) {
  const form = forms.inbound;
  return (
    <DomainPanel title="입고 처리" description="입고 지시를 만들고 실사 수량 기준으로 재고와 적치 작업을 생성합니다.">
      <QuickAction
        title="입고를 한 번에 완료"
        description="입고 지시 생성, 실사 처리, 입고 확정을 순서대로 실행하고 재고 화면으로 이동합니다."
        buttonLabel="입고 완료하고 재고 보기"
        onClick={async () => {
          const data = await run('입고 한번에 완료', async () => {
            const created = await api('/inbound-orders', {
              method: 'POST',
              body: JSON.stringify({
                warehouseId: Number(form.warehouseId),
                supplierId: Number(form.supplierId),
                scheduledDate: form.scheduledDate,
                items: [{ skuId: Number(form.skuId), skuName: form.skuName, orderedQty: Number(form.orderedQty) }],
              }),
            });
            const inboundOrderId = created.id;
            const inboundItemId = created.items?.[0]?.id;
            updateSection('inbound', {
              inboundOrderId: String(inboundOrderId),
              inboundItemId: String(inboundItemId),
            });
            await api(`/inbound-orders/${inboundOrderId}/receive`, {
              method: 'PATCH',
              body: JSON.stringify({ items: [{ inboundItemId, receivedQty: Number(form.receivedQty) }] }),
            });
            const confirmed = await api(`/inbound-orders/${inboundOrderId}/confirm`, {
              method: 'PATCH',
              body: JSON.stringify({ confirmedBy: Number(form.confirmedBy), memo: '관리자 콘솔 입고 확정' }),
            });
            updateSection('inventory', { warehouseId: form.warehouseId, skuId: form.skuId });
            updateSection('putaway', { warehouseId: form.warehouseId });
            await refreshDashboard();
            return { created, confirmed };
          });
          if (data) changeTab('inventory');
        }}
      />
      <StepBlock number="1" title="입고 지시 만들기" hint="공급사에서 들어올 SKU와 예정 수량을 등록합니다.">
        <FormGrid>
          <SelectField label="입고 창고" value={form.warehouseId} onChange={(v) => updateForm('inbound', 'warehouseId', v)} options={warehouseOptions} />
          <SelectField label="공급사" value={form.supplierId} onChange={(v) => updateForm('inbound', 'supplierId', v)} options={supplierOptions} />
          <Field label="입고 예정일" value={form.scheduledDate} onChange={(v) => updateForm('inbound', 'scheduledDate', v)} />
          <Field label="SKU 코드" value={form.skuId} onChange={(v) => updateForm('inbound', 'skuId', v)} />
          <Field label="상품명" value={form.skuName} onChange={(v) => updateForm('inbound', 'skuName', v)} />
          <Field label="지시 수량" value={form.orderedQty} onChange={(v) => updateForm('inbound', 'orderedQty', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('입고 지시 생성', async () => {
            const data = await api('/inbound-orders', {
              method: 'POST',
              body: JSON.stringify({
                warehouseId: Number(form.warehouseId),
                supplierId: Number(form.supplierId),
                scheduledDate: form.scheduledDate,
                items: [{ skuId: Number(form.skuId), skuName: form.skuName, orderedQty: Number(form.orderedQty) }],
              }),
            });
            updateForm('inbound', 'inboundOrderId', String(data?.id || form.inboundOrderId));
            updateForm('inbound', 'inboundItemId', String(data?.items?.[0]?.id || form.inboundItemId));
            return data;
          })}>입고 생성</ActionButton>
        </ActionRow>
      </StepBlock>
      <StepBlock number="2" title="실사 후 확정하기" hint="입고 생성 후 지시 번호와 품목 번호가 자동으로 채워집니다. 실제 입고 수량만 확인합니다.">
        <FormGrid>
          <Field label="입고 지시 번호" value={form.inboundOrderId} onChange={(v) => updateForm('inbound', 'inboundOrderId', v)} />
          <Field label="입고 품목 번호" value={form.inboundItemId} onChange={(v) => updateForm('inbound', 'inboundItemId', v)} />
          <Field label="실사 수량" value={form.receivedQty} onChange={(v) => updateForm('inbound', 'receivedQty', v)} />
          <SelectField label="확정 작업자" value={form.confirmedBy} onChange={(v) => updateForm('inbound', 'confirmedBy', v)} options={workerOptions} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('입고 조회', () => api(`/inbound-orders/${form.inboundOrderId}`))}>조회</ActionButton>
          <ActionButton onClick={() => run('실사 입고', () => api(`/inbound-orders/${form.inboundOrderId}/receive`, {
            method: 'PATCH',
            body: JSON.stringify({ items: [{ inboundItemId: Number(form.inboundItemId), receivedQty: Number(form.receivedQty) }] }),
          }))}>실사 처리</ActionButton>
          <ActionButton onClick={() => run('입고 확정', () => api(`/inbound-orders/${form.inboundOrderId}/confirm`, {
            method: 'PATCH',
            body: JSON.stringify({ confirmedBy: Number(form.confirmedBy), memo: '관리자 콘솔 입고 확정' }),
          }))}>입고 확정</ActionButton>
        </ActionRow>
      </StepBlock>
    </DomainPanel>
  );
}

function InventoryView({ forms, updateForm, run, setInventoryRows, changeTab }) {
  const form = forms.inventory;
  return (
    <DomainPanel title="재고 조회" description="가용 재고, 할당 재고, 위치별 재고와 변경 이력을 확인합니다.">
      <StepBlock number="1" title="현재 재고 보기" hint="먼저 창고 단위 목록을 조회하고, 필요하면 SKU나 이력으로 좁혀 봅니다.">
        <FormGrid>
          <SelectField label="조회 창고" value={form.warehouseId} onChange={(v) => updateForm('inventory', 'warehouseId', v)} options={warehouseOptions} />
          <Field label="SKU 코드" value={form.skuId} onChange={(v) => updateForm('inventory', 'skuId', v)} />
          <Field label="재고 번호" value={form.inventoryId} onChange={(v) => updateForm('inventory', 'inventoryId', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('재고 목록 조회', async () => {
            const rows = await api(`/inventories?warehouseId=${form.warehouseId}`);
            setInventoryRows(rows);
            if (rows?.[0]?.id) updateForm('inventory', 'inventoryId', String(rows[0].id));
            return rows;
          })}>목록 조회</ActionButton>
          <ActionButton onClick={() => run('SKU 재고 조회', () => api(`/inventories/${form.skuId}?warehouseId=${form.warehouseId}`))}>SKU 조회</ActionButton>
          <ActionButton onClick={() => run('재고 이력 조회', () => api(`/inventories/${form.inventoryId}/history`))}>이력 조회</ActionButton>
        </ActionRow>
      </StepBlock>
      <QuickAction
        title="재고 확인 후 적치로 이동"
        description="현재 창고 재고를 조회하고, 입고로 만들어진 적치 작업을 처리하러 이동합니다."
        buttonLabel="재고 확인하고 적치하기"
        onClick={async () => {
          const data = await run('재고 확인', async () => {
            const rows = await api(`/inventories?warehouseId=${form.warehouseId}`);
            setInventoryRows(rows);
            if (rows?.[0]?.id) updateForm('inventory', 'inventoryId', String(rows[0].id));
            return rows;
          });
          if (data) changeTab('putaway');
        }}
      />
      <StepBlock number="2" title="운영 조정하기" hint="실사 차이나 파손처럼 수동 보정이 필요한 경우에만 사용합니다.">
        <FormGrid>
          <Field label="조정 수량" value={form.adjustQty} onChange={(v) => updateForm('inventory', 'adjustQty', v)} />
          <Field label="조정 사유" value={form.reason} onChange={(v) => updateForm('inventory', 'reason', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('재고 조정', () => api('/inventories/adjust', {
            method: 'POST',
            body: JSON.stringify({ inventoryId: Number(form.inventoryId), adjustQty: Number(form.adjustQty), reason: form.reason }),
          }))}>재고 조정</ActionButton>
        </ActionRow>
      </StepBlock>
    </DomainPanel>
  );
}

function PutawayView({ forms, updateForm, run, setPutawayRows, changeTab, refreshDashboard }) {
  const form = forms.putaway;
  return (
    <DomainPanel title="적치 작업" description="입고 확정 후 생성된 작업을 시작하고 실제 위치에 적치합니다.">
      <QuickAction
        title="대기 적치를 한 번에 완료"
        description="대기 중인 적치 작업을 찾아 작업 시작과 위치 확정을 이어서 실행합니다."
        buttonLabel="적치 완료하고 출고로 이동"
        onClick={async () => {
          const data = await run('적치 한번에 완료', async () => {
            const rows = await api(`/putaway-tasks?warehouseId=${form.warehouseId}&status=PENDING`);
            const task = rows?.[0];
            if (!task) throw new Error('대기 중인 적치 작업이 없습니다. 먼저 입고를 완료해주세요.');
            updateForm('putaway', 'putawayTaskId', String(task.id));
            await api(`/putaway-tasks/${task.id}/start`, {
              method: 'PATCH',
              body: JSON.stringify({ assignedTo: Number(form.assignedTo) }),
            });
            const confirmed = await api(`/putaway-tasks/${task.id}/confirm`, {
              method: 'PATCH',
              body: JSON.stringify({ confirmedLocation: form.confirmedLocation }),
            });
            setPutawayRows([]);
            await refreshDashboard();
            return confirmed;
          });
          if (data) changeTab('outbound');
        }}
      />
      <StepBlock number="1" title="대기 작업 찾기" hint="입고 확정 후 생긴 적치 작업을 먼저 조회합니다.">
        <FormGrid>
          <SelectField label="작업 창고" value={form.warehouseId} onChange={(v) => updateForm('putaway', 'warehouseId', v)} options={warehouseOptions} />
          <Field label="적치 작업 번호" value={form.putawayTaskId} onChange={(v) => updateForm('putaway', 'putawayTaskId', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('적치 목록 조회', async () => {
            const rows = await api(`/putaway-tasks?warehouseId=${form.warehouseId}&status=PENDING`);
            setPutawayRows(rows);
            if (rows?.[0]?.id) updateForm('putaway', 'putawayTaskId', String(rows[0].id));
            return rows;
          })}>대기 조회</ActionButton>
        </ActionRow>
      </StepBlock>
      <StepBlock number="2" title="작업 시작 후 위치 확정" hint="작업자와 실제 적치 위치를 입력해 위치 재고를 반영합니다.">
        <FormGrid>
          <SelectField label="담당 작업자" value={form.assignedTo} onChange={(v) => updateForm('putaway', 'assignedTo', v)} options={workerOptions} />
          <Field label="확정 위치" value={form.confirmedLocation} onChange={(v) => updateForm('putaway', 'confirmedLocation', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('적치 시작', () => api(`/putaway-tasks/${form.putawayTaskId}/start`, {
            method: 'PATCH',
            body: JSON.stringify({ assignedTo: Number(form.assignedTo) }),
          }))}>작업 시작</ActionButton>
          <ActionButton onClick={() => run('적치 확정', () => api(`/putaway-tasks/${form.putawayTaskId}/confirm`, {
            method: 'PATCH',
            body: JSON.stringify({ confirmedLocation: form.confirmedLocation }),
          }))}>위치 확정</ActionButton>
        </ActionRow>
      </StepBlock>
    </DomainPanel>
  );
}

function OutboundView({ forms, updateForm, updateSection, run, changeTab }) {
  const form = forms.outbound;
  return (
    <DomainPanel title="출고 처리" description="출고 지시를 만들고 재고 할당 후 피킹 작업을 생성합니다.">
      <QuickAction
        title="출고를 한 번에 할당"
        description="출고 지시를 만들고 재고 할당까지 실행합니다. 할당이 끝나면 피킹 작업이 생깁니다."
        buttonLabel="출고 할당하고 피킹으로 이동"
        onClick={async () => {
          const data = await run('출고 한번에 할당', async () => {
            const created = await api('/outbound-orders', {
              method: 'POST',
              body: JSON.stringify({
                warehouseId: Number(form.warehouseId),
                orderId: `${form.orderId}-${Date.now()}`,
                items: [{ skuId: Number(form.skuId), requestedQty: Number(form.requestedQty), locationCode: form.locationCode }],
              }),
            });
            updateSection('outbound', { outboundOrderId: String(created.id) });
            updateSection('shipping', { outboundOrderId: String(created.id) });
            updateSection('returns', { outboundOrderId: String(created.id) });
            const allocated = await api(`/outbound-orders/${created.id}/allocate`, { method: 'PATCH' });
            return { created, allocated };
          });
          if (data) changeTab('picking');
        }}
      />
      <StepBlock number="1" title="출고 지시 만들기" hint="주문 기준으로 어떤 SKU를 어느 위치에서 피킹할지 등록합니다.">
        <FormGrid>
          <SelectField label="출고 창고" value={form.warehouseId} onChange={(v) => updateForm('outbound', 'warehouseId', v)} options={warehouseOptions} />
          <Field label="주문번호" value={form.orderId} onChange={(v) => updateForm('outbound', 'orderId', v)} />
          <Field label="SKU 코드" value={form.skuId} onChange={(v) => updateForm('outbound', 'skuId', v)} />
          <Field label="요청 수량" value={form.requestedQty} onChange={(v) => updateForm('outbound', 'requestedQty', v)} />
          <Field label="피킹 위치" value={form.locationCode} onChange={(v) => updateForm('outbound', 'locationCode', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('출고 지시 생성', async () => {
            const data = await api('/outbound-orders', {
              method: 'POST',
              body: JSON.stringify({
                warehouseId: Number(form.warehouseId),
                orderId: form.orderId,
                items: [{ skuId: Number(form.skuId), requestedQty: Number(form.requestedQty), locationCode: form.locationCode }],
              }),
            });
            updateForm('outbound', 'outboundOrderId', String(data?.id || form.outboundOrderId));
            updateForm('shipping', 'outboundOrderId', String(data?.id || forms.shipping.outboundOrderId));
            updateForm('returns', 'outboundOrderId', String(data?.id || forms.returns.outboundOrderId));
            return data;
          })}>출고 생성</ActionButton>
        </ActionRow>
      </StepBlock>
      <StepBlock number="2" title="재고 할당과 출고 확정" hint="출고 생성 후 지시 번호가 자동으로 채워집니다. 먼저 할당한 뒤 피킹/송장 흐름으로 넘어갑니다.">
        <FormGrid>
          <Field label="출고 지시 번호" value={form.outboundOrderId} onChange={(v) => updateForm('outbound', 'outboundOrderId', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('출고 조회', () => api(`/outbound-orders/${form.outboundOrderId}`))}>조회</ActionButton>
          <ActionButton onClick={() => run('재고 할당', () => api(`/outbound-orders/${form.outboundOrderId}/allocate`, { method: 'PATCH' }))}>할당</ActionButton>
          <ActionButton onClick={() => run('출고 확정', () => api(`/outbound-orders/${form.outboundOrderId}/ship`, { method: 'PATCH' }))}>출고 확정</ActionButton>
          <ActionButton onClick={() => run('출고 취소', () => api(`/outbound-orders/${form.outboundOrderId}/cancel`, { method: 'PATCH' }))}>취소</ActionButton>
        </ActionRow>
      </StepBlock>
    </DomainPanel>
  );
}

function PickingView({ forms, updateForm, run, setPickingRows, changeTab }) {
  const form = forms.picking;
  return (
    <DomainPanel title="피킹 작업" description="출고 할당 후 생성된 피킹 작업을 조회하고 완료합니다.">
      <QuickAction
        title="피킹을 한 번에 완료"
        description="열린 피킹 웨이브와 작업을 찾아 담당 작업자 기준으로 완료 처리합니다."
        buttonLabel="피킹 완료하고 송장으로 이동"
        onClick={async () => {
          const data = await run('피킹 한번에 완료', async () => {
            const waves = await api(`/picking-waves?warehouseId=${form.warehouseId}&status=OPEN`);
            const wave = waves?.[0];
            if (!wave) throw new Error('열린 피킹 웨이브가 없습니다. 먼저 출고 할당을 완료해주세요.');
            updateForm('picking', 'pickingWaveId', String(wave.id));
            const tasks = await api(`/picking-waves/${wave.id}/tasks`);
            const task = tasks?.[0];
            if (!task) throw new Error('피킹 작업이 없습니다.');
            updateForm('picking', 'pickingTaskId', String(task.id));
            const picked = await api(`/picking-tasks/${task.id}/pick`, {
              method: 'PATCH',
              body: JSON.stringify({ assignedTo: Number(form.assignedTo) }),
            });
            setPickingRows(waves);
            return picked;
          });
          if (data) changeTab('shipping');
        }}
      />
      <StepBlock number="1" title="피킹 웨이브 확인" hint="출고 할당 후 열린 웨이브와 그 안의 작업 목록을 조회합니다.">
        <FormGrid>
          <SelectField label="작업 창고" value={form.warehouseId} onChange={(v) => updateForm('picking', 'warehouseId', v)} options={warehouseOptions} />
          <Field label="웨이브 번호" value={form.pickingWaveId} onChange={(v) => updateForm('picking', 'pickingWaveId', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('피킹 웨이브 조회', async () => {
            const rows = await api(`/picking-waves?warehouseId=${form.warehouseId}&status=OPEN`);
            setPickingRows(rows);
            if (rows?.[0]?.id) updateForm('picking', 'pickingWaveId', String(rows[0].id));
            return rows;
          })}>웨이브 조회</ActionButton>
          <ActionButton onClick={() => run('피킹 작업 조회', async () => {
            const rows = await api(`/picking-waves/${form.pickingWaveId}/tasks`);
            if (rows?.[0]?.id) updateForm('picking', 'pickingTaskId', String(rows[0].id));
            return rows;
          })}>작업 조회</ActionButton>
        </ActionRow>
      </StepBlock>
      <StepBlock number="2" title="작업 완료 처리" hint="작업 조회 후 피킹 작업 번호가 자동으로 채워집니다. 담당 작업자만 확인합니다.">
        <FormGrid>
          <Field label="피킹 작업 번호" value={form.pickingTaskId} onChange={(v) => updateForm('picking', 'pickingTaskId', v)} />
          <SelectField label="담당 작업자" value={form.assignedTo} onChange={(v) => updateForm('picking', 'assignedTo', v)} options={workerOptions} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('피킹 완료', () => api(`/picking-tasks/${form.pickingTaskId}/pick`, {
            method: 'PATCH',
            body: JSON.stringify({ assignedTo: Number(form.assignedTo) }),
          }))}>피킹 완료</ActionButton>
        </ActionRow>
      </StepBlock>
    </DomainPanel>
  );
}

function ShippingView({ forms, updateForm, run, changeTab }) {
  const form = forms.shipping;
  return (
    <DomainPanel title="송장 출력" description="출고 지시에 대한 송장을 생성하고 출력 상태를 관리합니다.">
      <QuickAction
        title="송장 출력까지 완료"
        description="송장을 만들고 출력 요청, 출력 완료 상태까지 순서대로 변경합니다."
        buttonLabel="송장 완료하고 반품으로 이동"
        onClick={async () => {
          const data = await run('송장 한번에 완료', async () => {
            const created = await api('/shipping-labels', {
              method: 'POST',
              body: JSON.stringify({
                outboundOrderId: Number(form.outboundOrderId),
                carrier: form.carrier,
                receiverName: form.receiverName,
                receiverPhone: form.receiverPhone,
                receiverAddress: form.receiverAddress,
              }),
            });
            updateForm('shipping', 'shippingLabelId', String(created.id));
            await api(`/shipping-labels/${created.id}/print`, { method: 'POST' });
            const printed = await api(`/shipping-labels/${created.id}/printed`, { method: 'PATCH' });
            return { created, printed };
          });
          if (data) changeTab('returns');
        }}
      />
      <StepBlock number="1" title="송장 생성" hint="출고 지시와 수령인 정보를 기준으로 송장 데이터를 만듭니다.">
        <FormGrid>
          <Field label="출고 지시 번호" value={form.outboundOrderId} onChange={(v) => updateForm('shipping', 'outboundOrderId', v)} />
          <Field label="택배사" value={form.carrier} onChange={(v) => updateForm('shipping', 'carrier', v)} />
          <Field label="수령인" value={form.receiverName} onChange={(v) => updateForm('shipping', 'receiverName', v)} />
          <Field label="연락처" value={form.receiverPhone} onChange={(v) => updateForm('shipping', 'receiverPhone', v)} />
          <Field label="주소" value={form.receiverAddress} onChange={(v) => updateForm('shipping', 'receiverAddress', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('송장 생성', async () => {
            const data = await api('/shipping-labels', {
              method: 'POST',
              body: JSON.stringify({
                outboundOrderId: Number(form.outboundOrderId),
                carrier: form.carrier,
                receiverName: form.receiverName,
                receiverPhone: form.receiverPhone,
                receiverAddress: form.receiverAddress,
              }),
            });
            updateForm('shipping', 'shippingLabelId', String(data?.id || form.shippingLabelId));
            return data;
          })}>송장 생성</ActionButton>
        </ActionRow>
      </StepBlock>
      <StepBlock number="2" title="출력 상태 관리" hint="송장 생성 후 송장 번호가 자동으로 채워집니다. 출력 요청과 완료 처리를 진행합니다.">
        <FormGrid>
          <Field label="송장 번호" value={form.shippingLabelId} onChange={(v) => updateForm('shipping', 'shippingLabelId', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('송장 조회', () => api(`/shipping-labels/${form.shippingLabelId}`))}>조회</ActionButton>
          <ActionButton onClick={() => run('출력 요청', () => api(`/shipping-labels/${form.shippingLabelId}/print`, { method: 'POST' }))}>출력 요청</ActionButton>
          <ActionButton onClick={() => run('출력 완료', () => api(`/shipping-labels/${form.shippingLabelId}/printed`, { method: 'PATCH' }))}>출력 완료</ActionButton>
        </ActionRow>
      </StepBlock>
    </DomainPanel>
  );
}

function ReturnsView({ forms, updateForm, updateSection, run, changeTab }) {
  const form = forms.returns;
  return (
    <DomainPanel title="반품 처리" description="반품 실사 결과에 따라 재고 복구 또는 불량 이력을 기록합니다.">
      <QuickAction
        title="반품을 한 번에 완료"
        description="반품 접수, 실사 처리, 검수 완료까지 실행하고 재고 복구 여부를 확인할 수 있게 합니다."
        buttonLabel="반품 완료하고 재고 보기"
        onClick={async () => {
          const data = await run('반품 한번에 완료', async () => {
            const created = await api('/return-orders', {
              method: 'POST',
              body: JSON.stringify({
                outboundOrderId: Number(form.outboundOrderId),
                warehouseId: Number(form.warehouseId),
                reason: 'CUSTOMER_CHANGE',
                items: [{ skuId: Number(form.skuId), skuName: form.skuName, requestedQty: Number(form.requestedQty) }],
              }),
            });
            const returnOrderId = created.id;
            const returnItemId = created.items?.[0]?.id;
            updateSection('returns', {
              returnOrderId: String(returnOrderId),
              returnItemId: String(returnItemId),
            });
            await api(`/return-orders/${returnOrderId}/receive`, {
              method: 'PATCH',
              body: JSON.stringify({ items: [{ returnItemId, receivedQty: Number(form.receivedQty), condition: form.condition }] }),
            });
            const completed = await api(`/return-orders/${returnOrderId}/complete`, { method: 'PATCH' });
            updateSection('inventory', { warehouseId: form.warehouseId, skuId: form.skuId });
            return { created, completed };
          });
          if (data) changeTab('inventory');
        }}
      />
      <StepBlock number="1" title="반품 접수" hint="원 출고 지시를 기준으로 회수할 상품과 수량을 등록합니다.">
        <FormGrid>
          <Field label="출고 지시 번호" value={form.outboundOrderId} onChange={(v) => updateForm('returns', 'outboundOrderId', v)} />
          <SelectField label="반품 창고" value={form.warehouseId} onChange={(v) => updateForm('returns', 'warehouseId', v)} options={warehouseOptions} />
          <Field label="SKU 코드" value={form.skuId} onChange={(v) => updateForm('returns', 'skuId', v)} />
          <Field label="상품명" value={form.skuName} onChange={(v) => updateForm('returns', 'skuName', v)} />
          <Field label="반품 수량" value={form.requestedQty} onChange={(v) => updateForm('returns', 'requestedQty', v)} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('반품 접수', async () => {
            const data = await api('/return-orders', {
              method: 'POST',
              body: JSON.stringify({
                outboundOrderId: Number(form.outboundOrderId),
                warehouseId: Number(form.warehouseId),
                reason: 'CUSTOMER_CHANGE',
                items: [{ skuId: Number(form.skuId), skuName: form.skuName, requestedQty: Number(form.requestedQty) }],
              }),
            });
            updateForm('returns', 'returnOrderId', String(data?.id || form.returnOrderId));
            updateForm('returns', 'returnItemId', String(data?.items?.[0]?.id || form.returnItemId));
            return data;
          })}>반품 접수</ActionButton>
        </ActionRow>
      </StepBlock>
      <StepBlock number="2" title="검수 후 완료" hint="재판매 가능이면 재고 복구, 불량이면 불량 이력만 남깁니다.">
        <FormGrid>
          <Field label="반품 지시 번호" value={form.returnOrderId} onChange={(v) => updateForm('returns', 'returnOrderId', v)} />
          <Field label="반품 품목 번호" value={form.returnItemId} onChange={(v) => updateForm('returns', 'returnItemId', v)} />
          <Field label="회수 수량" value={form.receivedQty} onChange={(v) => updateForm('returns', 'receivedQty', v)} />
          <SelectField label="상품 상태" value={form.condition} onChange={(v) => updateForm('returns', 'condition', v)} options={['RESELLABLE', 'DEFECTIVE']} />
        </FormGrid>
        <ActionRow>
          <ActionButton onClick={() => run('반품 조회', () => api(`/return-orders/${form.returnOrderId}`))}>조회</ActionButton>
          <ActionButton onClick={() => run('반품 실사', () => api(`/return-orders/${form.returnOrderId}/receive`, {
            method: 'PATCH',
            body: JSON.stringify({ items: [{ returnItemId: Number(form.returnItemId), receivedQty: Number(form.receivedQty), condition: form.condition }] }),
          }))}>실사 처리</ActionButton>
          <ActionButton onClick={() => run('반품 완료', () => api(`/return-orders/${form.returnOrderId}/complete`, { method: 'PATCH' }))}>완료</ActionButton>
        </ActionRow>
      </StepBlock>
    </DomainPanel>
  );
}

function QuickAction({ title, description, buttonLabel, onClick }) {
  return (
    <section className="quick-action">
      <div>
        <strong>{title}</strong>
        <p>{description}</p>
      </div>
      <button className="primary-button large" onClick={onClick}>
        <ArrowRight size={16} />
        {buttonLabel}
      </button>
    </section>
  );
}

function DomainPanel({ title, description, children }) {
  return (
    <div className="domain-panel">
      <div className="section-title">
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
      <div className="domain-help">창고와 작업자는 이름으로 선택하고, 지시 번호와 작업 번호는 생성 또는 조회 결과에서 자동으로 채워집니다.</div>
      {children}
    </div>
  );
}

function StepBlock({ number, title, hint, children }) {
  return (
    <section className="step-block">
      <div className="step-header">
        <span>{number}</span>
        <div>
          <h3>{title}</h3>
          <p>{hint}</p>
        </div>
      </div>
      <div className="step-body">{children}</div>
    </section>
  );
}

function FormGrid({ children }) {
  return <div className="form-grid">{children}</div>;
}

function Field({ label, value, onChange }) {
  return (
    <label className="field">
      <span>{label}</span>
      <input value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function SelectField({ label, value, onChange, options }) {
  return (
    <label className="field">
      <span>{label}</span>
      <select value={value} onChange={(event) => onChange(event.target.value)}>
        {options.map((option) => {
          const normalized = typeof option === 'string' ? { label: option, value: option } : option;
          return (
            <option key={normalized.value} value={normalized.value}>{normalized.label}</option>
          );
        })}
      </select>
    </label>
  );
}

function ActionRow({ children }) {
  return <div className="action-row">{children}</div>;
}

function ActionButton({ children, onClick }) {
  return (
    <button className="primary-button" onClick={onClick}>
      <Send size={15} />
      {children}
    </button>
  );
}

function DataTable({ title, rows, columns }) {
  return (
    <div className="table-wrap">
      <div className="table-title">
        <h3>{title}</h3>
        <span>{rows.length}건</span>
      </div>
      <table>
        <thead>
          <tr>
            {columns.map(([key, label]) => <th key={key}>{label}</th>)}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="empty-cell">조회된 데이터가 없습니다.</td>
            </tr>
          ) : rows.map((row, index) => (
            <tr key={`${row.id || index}-${index}`}>
              {columns.map(([key]) => (
                <td key={key}>{formatCell(row[key])}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function formatCell(value) {
  if (value === null || value === undefined || value === '') return '-';
  return String(value);
}

function ResultPanel({ result }) {
  return (
    <aside className="result-panel">
      <div className="result-header">
        <h2>응답</h2>
        <ArrowRight size={18} />
      </div>
      {!result ? (
        <p className="muted">API 실행 결과가 여기에 표시됩니다.</p>
      ) : (
        <>
          <div className="result-label">{result.label}</div>
          <pre className={result.error ? 'error' : ''}>{JSON.stringify(result.error || result.data, null, 2)}</pre>
        </>
      )}
    </aside>
  );
}

createRoot(document.getElementById('root')).render(<App />);
