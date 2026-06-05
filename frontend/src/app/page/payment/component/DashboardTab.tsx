import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { cashBalanceApi, financialApi, extractErrorMessage } from "../../../service/api";
import type { FinancialDashboardData, FinancialMonthlyPoint } from "../../../service/types";
import { formatMoney, parseMoney } from "../../../service/formatters";
import { useToast } from "../../../component/notification/ToastProvider";

// ─── Constants ────────────────────────────────────────────────────────────────

const MONTH_ABR = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"];

function defaultDateRange(): { startDate: string; endDate: string } {
  const today = new Date();
  const start = new Date(today.getFullYear(), today.getMonth() - 11, 1);
  const fmt = (d: Date) => d.toISOString().slice(0, 10);
  return { startDate: fmt(start), endDate: fmt(today) };
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function kFmt(val: number): string {
  const sign = val < 0 ? "-" : "";
  const abs = Math.abs(val);
  if (abs >= 1_000_000) return `${sign}${(abs / 1_000_000).toFixed(1)}M`;
  if (abs >= 1_000) return `${sign}${(abs / 1_000).toFixed(0)}k`;
  return `${sign}${abs.toFixed(0)}`;
}

// ─── Bar Chart ────────────────────────────────────────────────────────────────

function MonthlyBarChart({ data, emptyLabel }: { data: FinancialMonthlyPoint[]; emptyLabel: string }) {
  if (data.length === 0) {
    return (
      <div className="flex items-center justify-center h-32 text-sm text-slate-400">
        {emptyLabel}
      </div>
    );
  }

  const W = 700;
  const H = 220;
  const PAD = { top: 20, right: 20, bottom: 50, left: 72 };
  const plotW = W - PAD.left - PAD.right;
  const plotH = H - PAD.top - PAD.bottom;

  const maxV = Math.max(...data.map((d) => d.net), 0);
  const minV = Math.min(...data.map((d) => d.net), 0);
  const range = maxV - minV || 1;

  const toY = (v: number) => PAD.top + ((maxV - v) / range) * plotH;
  const zeroY = toY(0);

  const slotW = plotW / data.length;
  const bw = Math.min(slotW * 0.65, 48);
  const bo = (slotW - bw) / 2;

  const numTicks = 4;
  const tickStep = range / numTicks;
  const ticks = Array.from({ length: numTicks + 1 }, (_, i) => minV + i * tickStep);

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="w-full" style={{ maxHeight: 240 }}>
      {ticks.map((v, i) => {
        const y = toY(v);
        const isZero = Math.abs(v) < tickStep * 0.01;
        return (
          <g key={i}>
            <line
              x1={PAD.left}
              y1={y}
              x2={W - PAD.right}
              y2={y}
              stroke={isZero ? "#94a3b8" : "#f1f5f9"}
              strokeWidth={isZero ? 1.5 : 1}
              strokeDasharray={isZero ? undefined : "4 2"}
            />
            <text x={PAD.left - 6} y={y + 4} textAnchor="end" fontSize="10" fill="#94a3b8">
              {kFmt(v)}
            </text>
          </g>
        );
      })}

      {!ticks.some((v) => Math.abs(v) < tickStep * 0.01) && (
        <line
          x1={PAD.left}
          y1={zeroY}
          x2={W - PAD.right}
          y2={zeroY}
          stroke="#94a3b8"
          strokeWidth="1.5"
        />
      )}

      {data.map((d, i) => {
        const bx = PAD.left + i * slotW + bo;
        const barH = Math.max((Math.abs(d.net) / range) * plotH, 1);
        const by = d.net >= 0 ? zeroY - barH : zeroY;
        const fill = d.net >= 0 ? "#22c55e" : "#f87171";
        const cx = bx + bw / 2;

        return (
          <g key={`${d.year}-${d.month}`}>
            <rect x={bx} y={by} width={bw} height={barH} fill={fill} rx="2" opacity="0.85" />
            <text
              x={cx}
              y={H - PAD.bottom + 14}
              textAnchor="middle"
              fontSize="9.5"
              fill="#94a3b8"
            >
              {MONTH_ABR[d.month - 1] ?? d.month}
            </text>
            <text
              x={cx}
              y={H - PAD.bottom + 27}
              textAnchor="middle"
              fontSize="9"
              fill="#c4cdd6"
            >
              {String(d.year).slice(2)}
            </text>
          </g>
        );
      })}

      <line
        x1={PAD.left}
        y1={PAD.top}
        x2={PAD.left}
        y2={H - PAD.bottom}
        stroke="#e2e8f0"
        strokeWidth="1"
      />
    </svg>
  );
}

// ─── Skeleton Card ────────────────────────────────────────────────────────────

function SkeletonCard({ className = "" }: { className?: string }) {
  return (
    <div className={`rounded-lg border border-slate-200 bg-slate-100 animate-pulse ${className}`} />
  );
}

// ─── Dashboard Tab ────────────────────────────────────────────────────────────

export default function DashboardTab() {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [data, setData] = useState<FinancialDashboardData | null>(null);

  const [dateRange, setDateRange] = useState(defaultDateRange);

  const [editingCash, setEditingCash] = useState(false);
  const [cashDraft, setCashDraft] = useState("");

  const fetchDashboard = useCallback(
    async (startDate: string, endDate: string) => {
      try {
        setLoading(true);
        const res = await financialApi.getFinancialDashboard(startDate, endDate);
        setData(res.data.data);
      } catch (err) {
        showToast(extractErrorMessage(err), "error");
      } finally {
        setLoading(false);
      }
    },
    [showToast]
  );

  useEffect(() => {
    fetchDashboard(dateRange.startDate, dateRange.endDate);
  }, [fetchDashboard, dateRange]);

  // ── Cash editing ─────────────────────────────────────────────────────────────

  const startEditCash = () => {
    setCashDraft((data?.cashBalanceAmount ?? 0).toFixed(2).replace(".", ","));
    setEditingCash(true);
  };

  const confirmEditCash = async () => {
    const val = parseMoney(cashDraft);
    const next = isNaN(val) ? (data?.cashBalanceAmount ?? 0) : Math.max(0, val);
    try {
      setSaving(true);
      await cashBalanceApi.update(next);
      setEditingCash(false);
      await fetchDashboard(dateRange.startDate, dateRange.endDate);
    } catch (err) {
      showToast(extractErrorMessage(err), "error");
    } finally {
      setSaving(false);
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────────

  if (loading || data === null) {
    return (
      <div className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <SkeletonCard className="h-28" />
          <SkeletonCard className="h-28" />
        </div>
        <div className="grid grid-cols-3 gap-4">
          <SkeletonCard className="h-24" />
          <SkeletonCard className="h-24" />
          <SkeletonCard className="h-24" />
        </div>
        <SkeletonCard className="h-72" />
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {/* Date range filter */}
      <div className="flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-slate-500">
            {t("payments.dashboard.periodStart")}
          </label>
          <input
            type="date"
            value={dateRange.startDate}
            max={dateRange.endDate}
            onChange={(e) => setDateRange((prev) => ({ ...prev, startDate: e.target.value }))}
            className="rounded border border-slate-200 bg-white px-2 py-1.5 text-sm text-slate-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-300"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-slate-500">
            {t("payments.dashboard.periodEnd")}
          </label>
          <input
            type="date"
            value={dateRange.endDate}
            min={dateRange.startDate}
            onChange={(e) => setDateRange((prev) => ({ ...prev, endDate: e.target.value }))}
            className="rounded border border-slate-200 bg-white px-2 py-1.5 text-sm text-slate-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-300"
          />
        </div>
        <button
          type="button"
          onClick={() => setDateRange(defaultDateRange())}
          className="self-end rounded border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-500 shadow-sm hover:bg-slate-100"
        >
          {t("payments.dashboard.resetPeriod")}
        </button>
      </div>

      {/* Row 1: Valor em Caixa + Patrimônio */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {/* Valor em Caixa */}
        <div className="rounded-lg border border-blue-200 bg-blue-50 px-5 py-4 shadow-sm">
          <div className="flex items-start justify-between gap-2">
            <span className="text-xs font-medium uppercase tracking-wide text-blue-700">
              {t("payments.dashboard.cashValue")}
            </span>
            {!editingCash && (
              <button
                type="button"
                onClick={startEditCash}
                className="mt-0.5 text-xs text-blue-500 hover:text-blue-700 underline leading-none"
              >
                {t("payments.dashboard.editBase")}
              </button>
            )}
          </div>

          {editingCash ? (
            <div className="mt-3 flex items-center gap-2 flex-wrap">
              <input
                type="text"
                value={cashDraft}
                onChange={(e) => setCashDraft(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") void confirmEditCash();
                  if (e.key === "Escape") setEditingCash(false);
                }}
                placeholder="0,00"
                className="w-36 rounded border border-blue-300 bg-white px-2 py-1 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-blue-300"
                autoFocus
                disabled={saving}
              />
              <button
                type="button"
                onClick={() => void confirmEditCash()}
                disabled={saving}
                className="text-xs font-medium text-green-700 hover:underline disabled:opacity-50"
              >
                {saving ? t("actions.saving") : t("actions.save")}
              </button>
              <button
                type="button"
                onClick={() => setEditingCash(false)}
                disabled={saving}
                className="text-xs text-slate-500 hover:underline disabled:opacity-50"
              >
                {t("actions.cancel")}
              </button>
            </div>
          ) : (
            <>
              <p
                className={`mt-2 text-2xl font-semibold tabular-nums ${
                  data.valorEmCaixa < 0 ? "text-red-700" : "text-blue-900"
                }`}
              >
                {formatMoney(data.valorEmCaixa)}
              </p>
              {data.cashBalanceAmount !== 0 && (
                <p className="mt-1.5 text-xs text-blue-500">
                  {t("payments.dashboard.cashBaseLabel")}: {formatMoney(data.cashBalanceAmount)}
                </p>
              )}
            </>
          )}
        </div>

        {/* Valor Patrimônio */}
        <div className="rounded-lg border border-violet-200 bg-violet-50 px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-violet-700">
            {t("payments.dashboard.patrimony")}
          </span>
          <p className="mt-2 text-2xl font-semibold tabular-nums text-violet-900">
            {formatMoney(data.patrimonio)}
          </p>
          <div className="mt-1.5 flex flex-wrap items-center gap-x-2 gap-y-0.5 text-xs text-violet-600">
            <span>
              {t("payments.dashboard.vehicleCountUnit")}: {data.activeVehiclesCount}
            </span>
            <span className="text-violet-400"> </span>
            <span>
              {t("payments.dashboard.vehiclesIn")}: {formatMoney(data.activeVehiclesTotalCost)}
            </span>
          </div>
        </div>
      </div>

      {/* Row 2: Custo Operacional + Lucro Vendas + Lucro Compras */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {/* Custo Total das Operações */}
        <div className="rounded-lg border border-red-200 bg-red-50 px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-red-700">
            {t("payments.dashboard.totalOperationsCost")}
          </span>
          <p className="mt-2 text-2xl font-semibold tabular-nums text-red-900">
            {formatMoney(data.totalPayments)}
          </p>
          <p className="mt-1 text-xs text-red-600">
            {t("payments.dashboard.totalOperationsCostDesc")}
          </p>
        </div>

        {/* Lucro Vendas */}
        <div className="rounded-lg border border-green-200 bg-green-50 px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-green-700">
            {t("payments.dashboard.salesProfit")}
          </span>
          <p className="mt-2 text-2xl font-semibold tabular-nums text-green-900">
            {formatMoney(data.lucroVendas)}
          </p>
          <p className="mt-1 text-xs text-green-600">
            {data.totalVehiclesSold} {t("payments.dashboard.vehiclesSold")}
          </p>
        </div>

        {/* Lucro Compras */}
        <div className="rounded-lg border border-slate-200 bg-white px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">
            {t("payments.dashboard.purchaseProfit")}
          </span>
          <p className="mt-2 text-2xl font-semibold tabular-nums text-slate-900">
            {formatMoney(data.lucroCompras)}
          </p>
          <p className="mt-1 text-xs text-slate-500">
            {t("payments.dashboard.totalAcquired")}: {data.totalVehiclesAcquired}
          </p>
        </div>
      </div>

      {/* Monthly evolution chart */}
      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-100 px-5 py-3">
          <h3 className="text-sm font-semibold text-slate-700">
            {t("payments.dashboard.monthlyEvolution")}
          </h3>
          <p className="mt-0.5 text-xs text-slate-400">
            {t("payments.dashboard.monthlyEvolutionDesc")}
          </p>
        </div>

        <div className="px-4 py-4">
          <MonthlyBarChart
            data={data.monthlyEvolution}
            emptyLabel={t("payments.empty")}
          />
        </div>

        {data.monthlyEvolution.length > 0 && (
          <div className="flex gap-4 border-t border-slate-100 px-5 py-3 text-xs text-slate-500">
            <span className="flex items-center gap-1.5">
              <span className="inline-block h-3 w-3 rounded-sm bg-green-500 opacity-85" />
              {t("payments.dashboard.legendPositive")}
            </span>
            <span className="flex items-center gap-1.5">
              <span className="inline-block h-3 w-3 rounded-sm bg-red-400 opacity-85" />
              {t("payments.dashboard.legendNegative")}
            </span>
          </div>
        )}
      </div>
    </div>
  );
}
