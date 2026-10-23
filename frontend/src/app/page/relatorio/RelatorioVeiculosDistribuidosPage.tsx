import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  MarcaItem,
  RelatorioVeiculosDistribuidos,
  ModeloItem,
  ParceiroItem,
  ListaParceirosResponse,
  GrupoParceiroRelatorio,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import DateInput from "../../component/input/DateInput";
import SelectInput from "../../component/input/SelectInput";
import ComboboxInput from "../../component/input/ComboboxInput";
import { fetchMarcas, fetchModelosByMarca } from "../../service/marcaModelos";
import { formatDate, formatMoney } from "../../service/formatters";

type TotalMode = "precoCompra" | "custoTotal";

const getCurrentMonthRange = () => {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1);
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  const toDateInput = (value: Date) => value.toISOString().slice(0, 10);
  return { dataInicio: toDateInput(start), dataFim: toDateInput(end) };
};

export default function RelatorioVeiculosDistribuidosPage() {
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [relatorio, setRelatorio] = useState<RelatorioVeiculosDistribuidos | null>(null);
  const [mode, setMode] = useState<TotalMode>("precoCompra");
  const [loading, setLoading] = useState(true);
  const [parceiros, setParceiros] = useState<ParceiroItem[]>([]);
  const [marcaOptions, setMarcaOptions] = useState<MarcaItem[]>([]);
  const [modeloOptions, setModeloOptions] = useState<ModeloItem[]>([]);
  const [filters, setFilters] = useState(() => {
    const range = getCurrentMonthRange();
    return {
      dataInicio: range.dataInicio,
      dataFim: range.dataFim,
      marca: "",
      modelo: "",
      parceiroId: "",
    };
  });
  const hasLoaded = useRef(false);

  const fetchRelatorio = async () => {
    try {
      setLoading(true);
      const response = await api.get<ApiResponse<RelatorioVeiculosDistribuidos>>(
        "/relatorios/veiculos-distribuidos",
        {
          params: {
            dataInicio: filters.dataInicio || undefined,
            dataFim: filters.dataFim || undefined,
            marca: filters.marca || undefined,
            modelo: filters.modelo || undefined,
            parceiroId: filters.parceiroId || undefined,
          },
        }
      );
      setRelatorio(response.data.data);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setLoading(false);
    }
  };

  const fetchParceiros = async () => {
    try {
      const response = await api.get<ApiResponse<ListaParceirosResponse>>("/parceiros");
      setParceiros(response.data.data.parceiros);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    }
  };

  useEffect(() => {
    if (hasLoaded.current) {
      return;
    }
    hasLoaded.current = true;
    fetchRelatorio();
    fetchParceiros();
    const loadMarcas = async () => {
      try {
        const marcas = await fetchMarcas();
        setMarcaOptions(marcas);
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    loadMarcas();
  }, []);

  useEffect(() => {
    const selectedMarca = marcaOptions.find((marca) => marca.nome === filters.marca);
    if (!selectedMarca) {
      setModeloOptions([]);
      return;
    }
    const loadModelos = async () => {
      try {
        const modelos = await fetchModelosByMarca(selectedMarca.id);
        setModeloOptions(modelos);
        if (filters.modelo && !modelos.some((modelo) => modelo.nome === filters.modelo)) {
          setFilters((prev) => ({ ...prev, modelo: "" }));
        }
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    loadModelos();
  }, [marcaOptions, filters.marca, filters.modelo, showToast]);

  const calcularParceiroTotal = (parceiro: GrupoParceiroRelatorio) => {
    if (mode === "precoCompra") {
      return parceiro.valorTotalVeiculosParceiro;
    }
    return parceiro.veiculos.reduce((total, veiculo) => total + veiculo.custoTotal, 0);
  };

  const overallTotal = relatorio
    ? relatorio.parceiros.reduce(
        (total, parceiro) => total + calcularParceiroTotal(parceiro),
        0
      )
    : 0;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold">{t("reports.distributed.title")}</h2>
          <p className="text-sm text-slate-500">
            {t("reports.distributed.subtitle")}
          </p>
        </div>
        <div className="flex items-center gap-2 rounded-full bg-slate-100 p-1 text-sm">
          <button
            type="button"
            onClick={() => setMode("precoCompra")}
            className={`rounded-full px-3 py-1 ${
              mode === "precoCompra"
                ? "bg-white shadow"
                : "text-slate-500"
            }`}
          >
            {t("reports.distributed.mode.purchasePrice")}
          </button>
          <button
            type="button"
            onClick={() => setMode("custoTotal")}
            className={`rounded-full px-3 py-1 ${
              mode === "custoTotal" ? "bg-white shadow" : "text-slate-500"
            }`}
          >
            {t("reports.distributed.mode.totalCost")}
          </button>
        </div>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="grid gap-4 md:grid-cols-5">
          <DateInput
            label={t("filters.startDate")}
            type="date"
            value={filters.dataInicio}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, dataInicio: event.target.value }))
            }
          />
          <DateInput
            label={t("filters.endDate")}
            type="date"
            value={filters.dataFim}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, dataFim: event.target.value }))
            }
          />
          <ComboboxInput
            label={t("filters.brand")}
            placeholder={t("placeholders.brand")}
            value={filters.marca}
            suggestions={marcaOptions.map((marca) => marca.nome)}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, marca: event.target.value }))
            }
          />
          <ComboboxInput
            label={t("filters.model")}
            placeholder={t("placeholders.model")}
            value={filters.modelo}
            suggestions={modeloOptions.map((modelo) => modelo.nome)}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, modelo: event.target.value }))
            }
          />
          <SelectInput
            label={t("filters.partner")}
            value={filters.parceiroId}
            options={[
              { value: "", label: t("filters.allPartners") },
              ...parceiros.map((parceiro) => ({
                value: parceiro.id,
                label: parceiro.nome,
              })),
            ]}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, parceiroId: event.target.value }))
            }
          />
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          <button
            type="button"
            onClick={fetchRelatorio}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
          >
            {t("actions.applyFilters")}
          </button>
          <button
            type="button"
            onClick={() => {
              const range = getCurrentMonthRange();
              setFilters({
                dataInicio: range.dataInicio,
                dataFim: range.dataFim,
                marca: "",
                modelo: "",
                parceiroId: "",
              });
              setTimeout(fetchRelatorio, 0);
            }}
            className="rounded-md border border-slate-200 px-4 py-2 text-sm"
          >
            {t("actions.clearFilters")}
          </button>
        </div>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-sm text-slate-500">
              {t("reports.distributed.overallTotal")}
            </div>
            <div className="text-xl font-semibold">
              {formatMoney(overallTotal)}
            </div>
          </div>
          <div className="text-sm text-slate-500">
            {t("reports.distributed.vehiclesCount", {
              count: relatorio?.quantidadeGeralVeiculos ?? 0,
            })}
          </div>
        </div>
      </div>

      {loading ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500 shadow-sm">
          {t("reports.distributed.loading")}
        </div>
      ) : null}

      {relatorio?.parceiros.map((parceiro) => (
        <div
          key={parceiro.parceiroId}
          className="rounded-lg border border-slate-200 bg-white shadow-sm"
        >
          <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
            <div>
              <h3 className="text-lg font-semibold">{parceiro.nomeParceiro}</h3>
              <p className="text-sm text-slate-500">
                {t("reports.distributed.vehiclesCount", {
                  count: parceiro.quantidadeVeiculosParceiro,
                })}
              </p>
            </div>
            <div className="text-right">
              <div className="text-xs text-slate-500">
                {t("reports.distributed.partnerTotal")}
              </div>
              <div className="text-base font-semibold">
                {formatMoney(calcularParceiroTotal(parceiro))}
              </div>
            </div>
          </div>
          <table className="min-w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-6 py-3">{t("reports.distributed.table.plate")}</th>
                <th className="px-6 py-3">{t("reports.distributed.table.brand")}</th>
                <th className="px-6 py-3">{t("reports.distributed.table.model")}</th>
                <th className="px-6 py-3">{t("reports.distributed.table.year")}</th>
                <th className="px-6 py-3">
                  {t("reports.distributed.table.distributedAt")}
                </th>
                <th className="px-6 py-3 text-right">
                  {t("reports.distributed.table.purchasePrice")}
                </th>
                <th className="px-6 py-3 text-right">
                  {t("reports.distributed.table.purchaseCommission")}
                </th>
                <th className="px-6 py-3 text-right">
                  {t("reports.distributed.table.totalCost")}
                </th>
              </tr>
            </thead>
            <tbody>
              {parceiro.veiculos.map((veiculo) => (
                <tr key={veiculo.veiculoId} className="border-t">
                  <td className="px-6 py-3">{veiculo.placa}</td>
                  <td className="px-6 py-3">{veiculo.marca}</td>
                  <td className="px-6 py-3">{veiculo.modelo}</td>
                  <td className="px-6 py-3">{veiculo.ano}</td>
                  <td className="px-6 py-3">
                    {formatDate(veiculo.distribuidoEm)}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(veiculo.precoCompra)}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(veiculo.comissaoCompra)}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(veiculo.custoTotal)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}

      {!loading && relatorio?.parceiros.length === 0 ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500">
          {t("reports.distributed.empty")}
        </div>
      ) : null}
    </div>
  );
}
