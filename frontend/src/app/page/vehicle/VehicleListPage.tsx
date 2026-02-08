import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  VehicleListResponse,
  VehicleStatus,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { formatMoney } from "../../service/formatters";

export default function VehicleListPage() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<VehicleStatus | "">("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState<VehicleListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const pageSize = 20;

  const params = useMemo(
    () => ({
      q: query || undefined,
      status: status || undefined,
      page,
      size: pageSize,
    }),
    [query, status, page]
  );

  const fetchVehicles = async () => {
    try {
      setLoading(true);
      const response = await api.get<ApiResponse<VehicleListResponse>>(
        "/vehicles",
        {
          params,
        }
      );
      setData(response.data.data);
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVehicles();
  }, [params.q, params.status, params.page]);

  const totalPages = data ? Math.ceil(data.total / data.size) : 1;
  const canGoPrev = page > 0;
  const canGoNext = data ? page + 1 < totalPages : false;
  const statuses: Array<{ value: VehicleStatus | ""; label: string }> = [
    { value: "", label: t("filters.all") },
    { value: "IN_LOT", label: t("status.IN_LOT") },
    { value: "IN_SERVICE", label: t("status.IN_SERVICE") },
    { value: "READY_FOR_DISTRIBUTION", label: t("status.READY_FOR_DISTRIBUTION") },
    { value: "DISTRIBUTED", label: t("status.DISTRIBUTED") },
    { value: "SOLD", label: t("status.SOLD") },
  ];
  const statusLabels: Record<VehicleStatus, string> = {
    IN_LOT: t("status.IN_LOT"),
    IN_SERVICE: t("status.IN_SERVICE"),
    READY_FOR_DISTRIBUTION: t("status.READY_FOR_DISTRIBUTION"),
    DISTRIBUTED: t("status.DISTRIBUTED"),
    SOLD: t("status.SOLD"),
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
          {statuses.map((option) => (
            <button
              key={option.value || "all"}
              type="button"
              onClick={() => {
                setStatus(option.value);
                setPage(0);
              }}
              className={`rounded-full px-3 py-1 text-xs font-medium ${
                status === option.value
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
          <table className="min-w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">{t("vehicles.table.plate")}</th>
                <th className="px-4 py-3">{t("vehicles.table.brand")}</th>
                <th className="px-4 py-3">{t("vehicles.table.model")}</th>
                <th className="px-4 py-3">{t("vehicles.table.year")}</th>
                <th className="px-4 py-3">{t("vehicles.table.status")}</th>
                <th className="px-4 py-3 text-right">{t("vehicles.table.purchasePrice")}</th>
                <th className="px-4 py-3 text-right">
                  {t("vehicles.table.purchaseCommission")}
                </th>
                <th className="px-4 py-3 text-right">{t("vehicles.table.servicesTotal")}</th>
                <th className="px-4 py-3 text-right">{t("vehicles.table.totalCost")}</th>
                <th className="px-4 py-3">{t("vehicles.table.partner")}</th>
                <th className="px-4 py-3">{t("vehicles.table.yardTime")}</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={11} className="px-4 py-6 text-center">
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
                    <td className="px-4 py-3 font-medium">
                      {vehicle.licensePlate}
                    </td>
                    <td className="px-4 py-3">{vehicle.brand}</td>
                    <td className="px-4 py-3">{vehicle.model}</td>
                    <td className="px-4 py-3">{vehicle.year}</td>
                    <td className="px-4 py-3">
                      {statusLabels[vehicle.status]}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {formatMoney(vehicle.purchasePrice)}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {formatMoney(vehicle.purchaseCommission)}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {formatMoney(vehicle.servicesTotal)}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {formatMoney(vehicle.totalCost)}
                    </td>
                    <td className="px-4 py-3">
                      {vehicle.assignedPartnerName ?? "-"}
                    </td>
                    <td className="px-4 py-3">
                      {vehicle.yardDays !== null && vehicle.yardDays !== undefined
                        ? t("units.days", { value: vehicle.yardDays })
                        : "-"}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={11} className="px-4 py-6 text-center text-slate-500">
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
