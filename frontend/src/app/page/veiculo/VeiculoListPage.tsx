import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  ItemListaVeiculo,
  ListaVeiculosResponse,
  StatusVeiculo,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { formatMoney } from "../../service/formatters";

const EM_SERVICO_FILTER = "__ON_SERVICE__" as const;
type FilterValue = StatusVeiculo | "" | typeof EM_SERVICO_FILTER;

export default function VeiculoListPage() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState<FilterValue>("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState<ListaVeiculosResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const pageSize = 20;

  const params = useMemo(() => {
    const isEmServicoFilter = filter === EM_SERVICO_FILTER;
    return {
      q: query || undefined,
      status: isEmServicoFilter || filter === "" ? undefined : filter,
      emServico: isEmServicoFilter ? true : undefined,
      page,
      size: pageSize,
    };
  }, [query, filter, page]);

  const fetchVeiculos = async () => {
    try {
      setLoading(true);
      const response = await api.get<ApiResponse<ListaVeiculosResponse>>(
        "/veiculos",
        { params }
      );
      setData(response.data.data);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVeiculos();
  }, [params.q, params.status, params.emServico, params.page]);

  const handleEmServicoClick = (
    event: React.MouseEvent,
    veiculo: ItemListaVeiculo
  ) => {
    event.stopPropagation();
    navigate(`/veiculos/${veiculo.id}`, { state: { tab: "servicos" } });
  };

  const totalPages = data ? Math.ceil(data.total / data.size) : 1;
  const canGoPrev = page > 0;
  const canGoNext = data ? page + 1 < totalPages : false;

  const filters: Array<{ value: FilterValue; label: string }> = [
    { value: "", label: t("filters.all") },
    { value: "NO_PATIO", label: t("status.NO_PATIO") },
    { value: EM_SERVICO_FILTER, label: t("status.EM_SERVICO") },
    { value: "DISTRIBUIDO", label: t("status.DISTRIBUIDO") },
    { value: "VENDIDO", label: t("status.VENDIDO") },
  ];

  const statusLabels: Record<StatusVeiculo, string> = {
    NO_PATIO: t("status.NO_PATIO"),
    PRONTO_PARA_DISTRIBUICAO: t("status.PRONTO_PARA_DISTRIBUICAO"),
    DISTRIBUIDO: t("status.DISTRIBUIDO"),
    VENDIDO: t("status.VENDIDO"),
  };

  const statusBadgeClass: Record<StatusVeiculo, string> = {
    NO_PATIO: "bg-yellow-100 text-yellow-700",
    PRONTO_PARA_DISTRIBUICAO: "bg-indigo-100 text-indigo-700",
    DISTRIBUIDO: "bg-blue-100 text-blue-700",
    VENDIDO: "bg-green-100 text-green-700",
  };

  const formatMargemLucro = (value?: number | null) => {
    if (value == null) return "-";
    const formatted = formatMoney(value);
    if (value > 0) return <span className="text-green-700">{formatted}</span>;
    if (value < 0) return <span className="text-red-700">{formatted}</span>;
    return formatted;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold">{t("vehicles.title")}</h2>
          <p className="text-sm text-slate-500">
            {t("vehicles.subtitle")}
          </p>
        </div>
        <button
          type="button"
          onClick={() => navigate("/veiculos/new")}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white shadow"
        >
          {t("vehicles.new")}
        </button>
      </div>
      <div className="flex flex-wrap gap-3">
        <input
          value={query}
          onChange={(event) => {
            setQuery(event.target.value);
            setPage(0);
          }}
          placeholder={t("vehicles.searchPlaceholder")}
          className="w-full max-w-sm rounded-md border border-slate-200 px-3 py-2 text-sm shadow-sm"
        />
        <div className="flex flex-wrap gap-2">
          {filters.map((option) => (
            <button
              key={option.value || "all"}
              type="button"
              onClick={() => {
                setFilter(option.value);
                setPage(0);
              }}
              className={`rounded-full px-3 py-1 text-xs font-medium ${
                filter === option.value
                  ? "bg-slate-900 text-white"
                  : "bg-slate-100 text-slate-600"
              }`}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>
      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="overflow-auto">
          <table className="min-w-full whitespace-nowrap text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-3 py-2">{t("vehicles.table.plate")}</th>
                <th className="px-3 py-2">{t("vehicles.table.model")}</th>
                <th className="px-3 py-2">{t("vehicles.table.year")}</th>
                <th className="px-3 py-2 text-right">{t("vehicles.table.sellingPrice")}</th>
                <th className="px-3 py-2 text-right">{t("vehicles.table.totalCost")}</th>
                <th className="px-3 py-2 text-right">{t("vehicles.table.profitMargin")}</th>
                <th className="px-3 py-2">{t("vehicles.table.status")}</th>
                <th className="px-3 py-2">{t("vehicles.table.purchaseTime")}</th>
                <th className="px-3 py-2 text-right">{t("vehicles.table.servicesTotal")}</th>
                <th className="px-3 py-2">{t("vehicles.table.daysOnService")}</th>
                <th className="px-3 py-2 text-center">{t("vehicles.table.onService")}</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={9} className="px-3 py-6 text-center">
                    {t("vehicles.loading")}
                  </td>
                </tr>
              ) : data?.items.length ? (
                data.items.map((veiculo) => (
                  <tr
                    key={veiculo.id}
                    className="cursor-pointer border-t hover:bg-slate-50"
                    onClick={() => navigate(`/veiculos/${veiculo.id}`)}
                  >
                    <td className="px-3 py-2 font-medium">
                      {veiculo.placa}
                    </td>
                    <td className="px-3 py-2 font-medium">
                      {veiculo.modelo}
                    </td>
                    <td className="px-3 py-2 font-medium">
                      {veiculo.ano}
                    </td>
                    <td className="px-3 py-2 text-right">
                      {veiculo.precoVenda != null
                        ? formatMoney(veiculo.precoVenda)
                        : "-"}
                    </td>
                    <td className="px-3 py-2 text-right">
                      {formatMoney(veiculo.custoTotal)}
                    </td>
                    <td className="px-3 py-2 text-right font-medium">
                      {formatMargemLucro(veiculo.margemLucro)}
                    </td>
                    <td className="px-3 py-2">
                      <span
                        className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${statusBadgeClass[veiculo.status]}`}
                      >
                        {statusLabels[veiculo.status]}
                      </span>
                    </td>
                    <td className="px-3 py-2 text-center">
                      {veiculo.diasTempoCompra != null
                        ? `${veiculo.diasTempoCompra} d`
                        : "-"}
                    </td>
                    <td className="px-3 py-2 text-right">
                      {formatMoney(veiculo.totalServicos)}
                    </td>
                    <td className="px-3 py-2 text-center">
                      {veiculo.diasEmServico !== null && veiculo.diasEmServico !== undefined
                          ? `${veiculo.diasEmServico} d`
                          : "-"}
                    </td>
                    <td className="px-3 py-2 text-center">
                      <button
                        type="button"
                        title={t("vehicles.onService.openServices")}
                        onClick={(e) => handleEmServicoClick(e, veiculo)}
                        className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${
                          veiculo.emServico
                            ? "bg-red-100 text-red-700"
                            : "bg-blue-100 text-blue-700"
                        }`}
                      >
                        <span
                          className={`inline-block h-2 w-2 rounded-full ${
                            veiculo.emServico ? "bg-red-500" : "bg-blue-500"
                          }`}
                        />
                        {veiculo.emServico
                          ? t("vehicles.onService.labelPreparation")
                          : t("vehicles.onService.labelReady")}
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={9} className="px-3 py-6 text-center text-slate-500">
                    {t("vehicles.empty")}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
      <div className="flex items-center justify-between text-sm text-slate-500">
        <span>{t("pagination.pageOf", { page: page + 1, total: totalPages || 1 })}</span>
        <div className="flex gap-2">
          <button
            type="button"
            disabled={!canGoPrev}
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            className="rounded-md border border-slate-200 px-3 py-1 disabled:cursor-not-allowed disabled:text-slate-300"
          >
            {t("pagination.previous")}
          </button>
          <button
            type="button"
            disabled={!canGoNext}
            onClick={() => setPage((prev) => prev + 1)}
            className="rounded-md border border-slate-200 px-3 py-1 disabled:cursor-not-allowed disabled:text-slate-300"
          >
            {t("pagination.next")}
          </button>
        </div>
      </div>
    </div>
  );
}
