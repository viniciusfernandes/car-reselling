import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import type {
  ApiResponse,
  MarcaItem,
  ModeloItem,
  ParceiroItem,
  ListaParceirosResponse,
  RelatorioVeiculosVendidos,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import ComboboxInput from "../../component/input/ComboboxInput";
import SelectInput from "../../component/input/SelectInput";
import { fetchMarcas, fetchModelosByMarca } from "../../service/marcaModelos";
import { formatMoney } from "../../service/formatters";

// ─── Constants ────────────────────────────────────────────────────────────────

const MONTH_ABR = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"];

// ─── Helpers ──────────────────────────────────────────────────────────────────

function kFmt(val: number): string {
  const sign = val < 0 ? "-" : "";
  const abs = Math.abs(val);
  if (abs >= 1_000_000) return `${sign}${(abs / 1_000_000).toFixed(1)}M`;
  if (abs >= 1_000) return `${sign}${(abs / 1_000).toFixed(0)}k`;
  return `${sign}${abs.toFixed(0)}`;
}

function defaultDateRange(): { dataInicio: string; dataFim: string } {
  const today = new Date();
  const start = new Date(today.getFullYear(), today.getMonth(), 1);
  const fmt = (d: Date) => d.toISOString().slice(0, 10);
  return { dataInicio: fmt(start), dataFim: fmt(today) };
}

function defaultChartRange(): { dataInicio: string; dataFim: string } {
  const today = new Date();
  const start = new Date(today.getFullYear(), today.getMonth() - 11, 1);
  const fmt = (d: Date) => d.toISOString().slice(0, 10);
  return { dataInicio: fmt(start), dataFim: fmt(today) };
}

// ─── Bar Chart ────────────────────────────────────────────────────────────────

type MonthlyBarPoint = { ano: number; mes: number; net: number };

function MonthlyBarChart({ data, emptyLabel }: { data: MonthlyBarPoint[]; emptyLabel: string }) {
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
          <g key={`${d.ano}-${d.mes}`}>
            <rect x={bx} y={by} width={bw} height={barH} fill={fill} rx="2" opacity="0.85" />
            <text
              x={cx}
              y={H - PAD.bottom + 14}
              textAnchor="middle"
              fontSize="9.5"
              fill="#94a3b8"
            >
              {MONTH_ABR[d.mes - 1] ?? d.mes}
            </text>
            <text
              x={cx}
              y={H - PAD.bottom + 27}
              textAnchor="middle"
              fontSize="9"
              fill="#c4cdd6"
            >
              {String(d.ano).slice(2)}
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

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function RelatorioVeiculosVendidosPage() {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [relatorio, setRelatorio] = useState<RelatorioVeiculosVendidos | null>(null);
  const [loading, setLoading] = useState(true);
  const [parceiros, setParceiros] = useState<ParceiroItem[]>([]);
  const [marcaOptions, setMarcaOptions] = useState<MarcaItem[]>([]);
  const [modeloOptions, setModeloOptions] = useState<ModeloItem[]>([]);

  const [dateRange, setDateRange] = useState(defaultDateRange);
  const [chartRange, setChartRange] = useState(defaultChartRange);
  const [extraFilters, setExtraFilters] = useState({ marca: "", modelo: "", parceiroId: "" });

  const hasLoaded = useRef(false);

  const fetchRelatorio = async (
    dataInicio: string,
    dataFim: string,
    marca: string,
    modelo: string,
    parceiroId: string
  ) => {
    try {
      setLoading(true);
      const response = await api.get<ApiResponse<RelatorioVeiculosVendidos>>("/relatorios/veiculos-vendidos", {
        params: {
          dataInicio: dataInicio || undefined,
          dataFim: dataFim || undefined,
          marca: marca || undefined,
          modelo: modelo || undefined,
          parceiroId: parceiroId || undefined,
        },
      });
      setRelatorio(response.data.data);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (hasLoaded.current) return;
    hasLoaded.current = true;

    const range = defaultDateRange();
    fetchRelatorio(range.dataInicio, range.dataFim, "", "", "");

    const fetchParceirosData = async () => {
      try {
        const response = await api.get<ApiResponse<ListaParceirosResponse>>("/parceiros");
        setParceiros(response.data.data.parceiros);
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    fetchParceirosData();

    const loadMarcas = async () => {
      try {
        const marcas = await fetchMarcas();
        setMarcaOptions(marcas);
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    loadMarcas();
  }, [showToast]);

  useEffect(() => {
    const selectedMarca = marcaOptions.find((b) => b.nome === extraFilters.marca);
    if (!selectedMarca) {
      setModeloOptions([]);
      return;
    }
    const loadModelos = async () => {
      try {
        const modelos = await fetchModelosByMarca(selectedMarca.id);
        setModeloOptions(modelos);
        if (extraFilters.modelo && !modelos.some((m) => m.nome === extraFilters.modelo)) {
          setExtraFilters((prev) => ({ ...prev, modelo: "" }));
        }
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    loadModelos();
  }, [marcaOptions, extraFilters.marca, extraFilters.modelo, showToast]);

  // ── Histogram: lucro por mês ────────────────────────────────────────────────

  const histogramData: MonthlyBarPoint[] = (() => {
    if (!relatorio?.veiculos?.length) return [];

    const start = new Date(chartRange.dataInicio);
    const end = new Date(chartRange.dataFim);
    if (isNaN(start.getTime()) || isNaN(end.getTime()) || start > end) return [];

    const map: Record<string, MonthlyBarPoint> = {};
    const cursor = new Date(start.getFullYear(), start.getMonth(), 1);
    const endMonth = new Date(end.getFullYear(), end.getMonth(), 1);

    while (cursor <= endMonth) {
      const key = `${cursor.getFullYear()}-${cursor.getMonth() + 1}`;
      map[key] = { ano: cursor.getFullYear(), mes: cursor.getMonth() + 1, net: 0 };
      cursor.setMonth(cursor.getMonth() + 1);
    }

    for (const veiculo of relatorio.veiculos) {
      if (!veiculo.dataVenda) continue;
      const date = new Date(veiculo.dataVenda);
      if (isNaN(date.getTime())) continue;
      const key = `${date.getFullYear()}-${date.getMonth() + 1}`;
      if (key in map) {
        map[key].net += veiculo.lucro;
      }
    }

    return Object.values(map);
  })();

  const applyFilters = () => {
    fetchRelatorio(
      dateRange.dataInicio,
      dateRange.dataFim,
      extraFilters.marca,
      extraFilters.modelo,
      extraFilters.parceiroId
    );
  };

  const resetFilters = () => {
    const range = defaultDateRange();
    setDateRange(range);
    setExtraFilters({ marca: "", modelo: "", parceiroId: "" });
    fetchRelatorio(range.dataInicio, range.dataFim, "", "", "");
  };

  // ── Skeleton ──────────────────────────────────────────────────────────────

  if (loading && relatorio === null) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-xl font-semibold">{t("reports.sales.title")}</h2>
          <p className="text-sm text-slate-500">{t("reports.sales.subtitle")}</p>
        </div>
        <div className="space-y-4">
          <SkeletonCard className="h-16" />
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
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold">{t("reports.sales.title")}</h2>
        <p className="text-sm text-slate-500">{t("reports.sales.subtitle")}</p>
      </div>

      {/* ── Filtros de data + extras ────────────────────────────────────────── */}
      <div className="flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-slate-500">
            {t("reports.sales.periodStart")}
          </label>
          <input
            type="date"
            value={dateRange.dataInicio}
            max={dateRange.dataFim}
            onChange={(e) => setDateRange((prev) => ({ ...prev, dataInicio: e.target.value }))}
            className="rounded border border-slate-200 bg-white px-2 py-1.5 text-sm text-slate-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-300"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-slate-500">
            {t("reports.sales.periodEnd")}
          </label>
          <input
            type="date"
            value={dateRange.dataFim}
            min={dateRange.dataInicio}
            onChange={(e) => setDateRange((prev) => ({ ...prev, dataFim: e.target.value }))}
            className="rounded border border-slate-200 bg-white px-2 py-1.5 text-sm text-slate-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-300"
          />
        </div>
        <ComboboxInput
          label={t("filters.brand")}
          placeholder={t("placeholders.brand")}
          value={extraFilters.marca}
          suggestions={marcaOptions.map((b) => b.nome)}
          onChange={(e) => setExtraFilters((prev) => ({ ...prev, marca: e.target.value }))}
        />
        <ComboboxInput
          label={t("filters.model")}
          placeholder={t("placeholders.model")}
          value={extraFilters.modelo}
          suggestions={modeloOptions.map((m) => m.nome)}
          onChange={(e) => setExtraFilters((prev) => ({ ...prev, modelo: e.target.value }))}
        />
        <SelectInput
          label={t("filters.partner")}
          value={extraFilters.parceiroId}
          options={[
            { value: "", label: t("filters.allPartners") },
            ...parceiros.map((p) => ({ value: p.id, label: p.nome })),
          ]}
          onChange={(e) => setExtraFilters((prev) => ({ ...prev, parceiroId: e.target.value }))}
        />
        <div className="flex gap-2 self-end">
          <button
            type="button"
            onClick={applyFilters}
            className="rounded border border-slate-900 bg-slate-900 px-3 py-1.5 text-xs font-medium text-white shadow-sm hover:bg-slate-700"
          >
            {t("actions.applyFilters")}
          </button>
          <button
            type="button"
            onClick={resetFilters}
            className="self-end rounded border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-500 shadow-sm hover:bg-slate-100"
          >
            {t("reports.sales.resetPeriod")}
          </button>
        </div>
      </div>

      {/* ── Row 1: Lucro Vendas + Valor Total Vendido ───────────────────────── */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {/* Lucro Vendas */}
        <div className="rounded-lg border border-green-200 bg-green-50 px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-green-700">
            {t("reports.sales.totals.profit")}
          </span>
          <div className="mt-2 flex flex-wrap items-end gap-x-4 gap-y-1">
            <div>
              <p className="text-2xl font-semibold tabular-nums text-green-900">
                {formatMoney(relatorio?.lucroAntesImpostos ?? 0)}
              </p>
              <p className="mt-0.5 text-xs text-green-600">
                {t("reports.sales.totals.profitBeforeTaxes")}
              </p>
            </div>
            <div className="border-l border-green-200 pl-4">
              <p className="text-base font-semibold tabular-nums text-green-700">
                {formatMoney(relatorio?.lucro ?? 0)}
              </p>
              <p className="mt-0.5 text-xs text-green-600">
                {t("reports.sales.totals.profitAfterTaxes")}
              </p>
            </div>
          </div>
          <p className="mt-2 text-xs text-green-600">
            {relatorio?.totalVeiculosVendidos ?? 0} {t("reports.sales.vehiclesSold")}
          </p>
        </div>

        {/* Valor Total Vendido */}
        <div className="rounded-lg border border-violet-200 bg-violet-50 px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-violet-700">
            {t("reports.sales.totals.soldValue")}
          </span>
          <p className="mt-2 text-2xl font-semibold tabular-nums text-violet-900">
            {formatMoney(relatorio?.valorTotalVendido ?? 0)}
          </p>
          <p className="mt-1.5 text-xs text-violet-600">
            {t("reports.sales.totals.vehicles")}: {relatorio?.totalVeiculosVendidos ?? 0}
          </p>
        </div>
      </div>

      {/* ── Row 2: Impostos + Serviços + Comissão ──────────────────────────── */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {/* Impostos */}
        <div className="rounded-lg border border-red-200 bg-red-50 px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-red-700">
            {t("reports.sales.totals.taxes")}
          </span>
          <p className="mt-2 text-2xl font-semibold tabular-nums text-red-900">
            {formatMoney(relatorio?.valorTotalImpostos ?? 0)}
          </p>
          <p className="mt-1 text-xs text-red-600">
            {t("reports.sales.totals.taxesDesc")}
          </p>
        </div>

        {/* Serviços */}
        <div className="rounded-lg border border-slate-200 bg-white px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">
            {t("reports.sales.totals.services")}
          </span>
          <p className="mt-2 text-2xl font-semibold tabular-nums text-slate-900">
            {formatMoney(relatorio?.valorTotalServicos ?? 0)}
          </p>
        </div>

        {/* Comissão */}
        <div className="rounded-lg border border-blue-200 bg-blue-50 px-5 py-4 shadow-sm">
          <span className="text-xs font-medium uppercase tracking-wide text-blue-700">
            {t("reports.sales.totals.commission")}
          </span>
          <p className="mt-2 text-2xl font-semibold tabular-nums text-blue-900">
            {formatMoney(relatorio?.valorTotalComissoes ?? 0)}
          </p>
        </div>
      </div>

      {/* ── Histograma de Lucro Mensal ──────────────────────────────────────── */}
      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-100 px-5 py-3">
          <h3 className="text-sm font-semibold text-slate-700">
            {t("reports.sales.histogram.title")}
          </h3>
          <p className="mt-0.5 text-xs text-slate-400">
            {t("reports.sales.histogram.subtitle")}
          </p>
        </div>

        <div className="px-4 py-4">
          <MonthlyBarChart
            data={histogramData}
            emptyLabel={t("reports.sales.histogram.empty")}
          />
        </div>

        {histogramData.length > 0 && (
          <div className="flex gap-4 border-t border-slate-100 px-5 py-3 text-xs text-slate-500">
            <span className="flex items-center gap-1.5">
              <span className="inline-block h-3 w-3 rounded-sm bg-green-500 opacity-85" />
              {t("reports.sales.histogram.legendPositive")}
            </span>
            <span className="flex items-center gap-1.5">
              <span className="inline-block h-3 w-3 rounded-sm bg-red-400 opacity-85" />
              {t("reports.sales.histogram.legendNegative")}
            </span>
          </div>
        )}
      </div>

      {/* ── Tabela de veículos ──────────────────────────────────────────────── */}
      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-6 py-3">{t("reports.sales.table.plate")}</th>
              <th className="px-6 py-3">{t("reports.sales.table.model")}</th>
              <th className="px-6 py-3 text-right">{t("reports.sales.table.year")}</th>
              <th className="px-6 py-3 text-right">{t("reports.sales.table.purchasePrice")}</th>
              <th className="px-6 py-3 text-right">{t("reports.sales.table.soldAt")}</th>
              <th className="px-6 py-3 text-right">{t("reports.sales.table.sellingPrice")}</th>
              <th className="px-6 py-3 text-right">{t("reports.sales.table.totalTaxes")}</th>
              <th className="px-6 py-3 text-right">{t("reports.sales.table.servicesTotal")}</th>
              <th className="px-6 py-3 text-right">{t("reports.sales.table.commission")}</th>
              <th className="px-6 py-3 text-right">{t("reports.sales.table.commissionRate")}</th>
              <th className="px-6 py-3 text-right whitespace-nowrap">
                {t("reports.sales.table.profit")}
              </th>
              <th className="px-6 py-3 text-right whitespace-nowrap">
                {t("reports.sales.table.profitBeforeTaxes")}
              </th>
            </tr>
          </thead>
          <tbody>
            {relatorio?.veiculos.length ? (
              relatorio.veiculos.map((veiculo) => (
                <tr key={veiculo.veiculoId} className="border-t">
                  <td className="px-6 py-3">{veiculo.placa}</td>
                  <td className="px-6 py-3">{veiculo.modelo}</td>
                  <td className="px-6 py-3 text-right">{veiculo.ano}</td>
                  <td className="px-6 py-3 text-right">{formatMoney(veiculo.precoCompra)}</td>
                  <td className="px-6 py-3 text-right">{veiculo.dataVenda ?? "—"}</td>
                  <td className="px-6 py-3 text-right">{formatMoney(veiculo.precoVenda)}</td>
                  <td className="px-6 py-3 text-right">{formatMoney(veiculo.totalImpostos)}</td>
                  <td className="px-6 py-3 text-right">{formatMoney(veiculo.totalServicos)}</td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(veiculo.comissaoCompra)}
                  </td>
                  <td className="px-6 py-3 text-center">
                    {veiculo.taxaComissao != null
                      ? `${veiculo.taxaComissao.toFixed(2)}%`
                      : "—"}
                  </td>
                  <td
                    className={`px-6 py-3 text-right font-medium whitespace-nowrap ${
                      veiculo.lucro < 0 ? "text-red-600" : "text-green-700"
                    }`}
                  >
                    {formatMoney(veiculo.lucro)}
                  </td>
                  <td
                      className={`px-6 py-3 text-right font-medium whitespace-nowrap ${
                          veiculo.profitBeforeTaxes < 0 ? "text-red-600" : "text-green-700"
                      }`}
                  >
                    {formatMoney(veiculo.profitBeforeTaxes)}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={11} className="px-6 py-6 text-center text-slate-500">
                  {t("reports.sales.empty")}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
