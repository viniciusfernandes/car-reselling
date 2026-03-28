import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  VehicleListItem,
  VehicleListResponse,
  VehicleStatus,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { formatMoney } from "../../service/formatters";

const ON_SERVICE_FILTER = "__ON_SERVICE__" as const;
type FilterValue = VehicleStatus | "" | typeof ON_SERVICE_FILTER;

export default function VehicleListPage() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState<FilterValue>("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState<VehicleListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [togglingId, setTogglingId] = useState<string | null>(null);
  const pageSize = 20;

  const params = useMemo(() => {
    const isOnServiceFilter = filter === ON_SERVICE_FILTER;
    return {
      q: query || undefined,
      status: isOnServiceFilter || filter === "" ? undefined : filter,
      onService: isOnServiceFilter ? true : undefined,
      page,
      size: pageSize,
    };
  }, [query, filter, page]);

  const fetchVehicles = async () => {
    try {
      setLoading(true);
      const response = await api.get<ApiResponse<VehicleListResponse>>(
        "/vehicles",
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
    fetchVehicles();
  }, [params.q, params.status, params.onService, params.page]);

  const handleToggleOnService = async (
    event: React.MouseEvent,
    vehicle: VehicleListItem
  ) => {
    event.stopPropagation();
    if (togglingId === vehicle.id) return;
    setTogglingId(vehicle.id);
    try {
      await api.post(`/vehicles/${vehicle.id}/on-service/toggle`);
      await fetchVehicles();
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setTogglingId(null);
    }
  };

  const totalPages = data ? Math.ceil(data.total / data.size) : 1;
  const canGoPrev = page > 0;
  const canGoNext = data ? page + 1 < totalPages : false;

  const filters: Array<{ value: FilterValue; label: string }> = [
    { value: "", label: t("filters.all") },
    { value: "IN_LOT", label: t("status.IN_LOT") },
    { value: ON_SERVICE_FILTER, label: t("status.ON_SERVICE") },
    { value: "DISTRIBUTED", label: t("status.DISTRIBUTED") },
    { value: "SOLD", label: t("status.SOLD") },
  ];

  const statusLabels: Record<VehicleStatus, string> = {
    IN_LOT: t("status.IN_LOT"),
    READY_FOR_DISTRIBUTION: t("status.READY_FOR_DISTRIBUTION"),
    DISTRIBUTED: t("status.DISTRIBUTED"),
    SOLD: t("status.SOLD"),
  };

  const statusBadgeClass: Record<VehicleStatus, string> = {
    IN_LOT: "bg-yellow-100 text-yellow-700",
    READY_FOR_DISTRIBUTION: "bg-indigo-100 text-indigo-700",
    DISTRIBUTED: "bg-blue-100 text-blue-700",
    SOLD: "bg-green-100 text-green-700",
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
          onClick={() => navigate("/vehicles/new")}
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
                <th className="px-3 py-2 text-right">{t("vehicles.table.purchasePrice")}</th>
                <th className="px-3 py-2 text-right">
                  {t("vehicles.table.purchaseCommission")}
                </th>
                <th className="px-3 py-2 text-right">{t("vehicles.table.servicesTotal")}</th>
                <th className="px-3 py-2 text-right">{t("vehicles.table.totalCost")}</th>
                <th className="px-3 py-2">{t("vehicles.table.partner")}</th>
                <th className="px-3 py-2">{t("vehicles.table.yardTime")}</th>
                <th className="px-3 py-2">{t("vehicles.table.status")}</th>
                <th className="px-3 py-2 text-center">{t("vehicles.table.onService")}</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={11} className="px-3 py-6 text-center">
                    {t("vehicles.loading")}
                  </td>
                </tr>
              ) : data?.items.length ? (
                data.items.map((vehicle) => (
                  <tr
                    key={vehicle.id}
                    className="cursor-pointer border-t hover:bg-slate-50"
                    onClick={() => navigate(`/vehicles/${vehicle.id}`)}
                  >
                    <td className="px-3 py-2 font-medium">
                      {vehicle.licensePlate}
                    </td>
                    <td className="px-3 py-2">{vehicle.model}</td>
                    <td className="px-3 py-2">{vehicle.year}</td>
                    <td className="px-3 py-2 text-right">
                      {formatMoney(vehicle.purchasePrice)}
                    </td>
                    <td className="px-3 py-2 text-right">
                      {formatMoney(vehicle.purchaseCommission)}
                    </td>
                    <td className="px-3 py-2 text-right">
                      {formatMoney(vehicle.servicesTotal)}
                    </td>
                    <td className="px-3 py-2 text-right">
                      {formatMoney(vehicle.totalCost)}
                    </td>
                    <td className="px-3 py-2">
                      {vehicle.assignedPartnerName ?? "-"}
                    </td>
                    <td className="px-3 py-2">
                      {vehicle.yardDays !== null && vehicle.yardDays !== undefined
                        ? t("units.days", { value: vehicle.yardDays })
                        : "-"}
                    </td>
                    <td className="px-3 py-2">
                      <span
                        className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${statusBadgeClass[vehicle.status]}`}
                      >
                        {statusLabels[vehicle.status]}
                      </span>
                    </td>
                    <td className="px-3 py-2 text-center">
                      <button
                        type="button"
                        title={
                          vehicle.onService
                            ? t("vehicles.onService.disable")
                            : t("vehicles.onService.enable")
                        }
                        disabled={togglingId === vehicle.id}
                        onClick={(e) => handleToggleOnService(e, vehicle)}
                        className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium transition-opacity disabled:opacity-50 ${
                          vehicle.onService
                            ? "bg-red-100 text-red-700"
                            : "bg-blue-100 text-blue-700"
                        }`}
                      >
                        <span
                          className={`inline-block h-2 w-2 rounded-full ${
                            vehicle.onService ? "bg-red-500" : "bg-blue-500"
                          }`}
                        />
                        {vehicle.onService
                          ? t("vehicles.onService.labelPreparation")
                          : t("vehicles.onService.labelReady")}
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={11} className="px-3 py-6 text-center text-slate-500">
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
