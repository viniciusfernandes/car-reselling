import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  DistributedVehiclesReport,
  PartnerItem,
  PartnerListResponse,
  ReportPartnerGroup,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import DateInput from "../../component/input/DateInput";
import SelectInput from "../../component/input/SelectInput";
import ComboboxInput from "../../component/input/ComboboxInput";
import { fetchVehicleSuggestions } from "../../service/vehicleSuggestions";
import { formatDate, formatMoney } from "../../service/formatters";

type TotalMode = "purchasePrice" | "totalCost";

const getCurrentMonthRange = () => {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1);
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  const toDateInput = (value: Date) => value.toISOString().slice(0, 10);
  return { startDate: toDateInput(start), endDate: toDateInput(end) };
};

export default function DistributedVehiclesReportPage() {
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [report, setReport] = useState<DistributedVehiclesReport | null>(null);
  const [mode, setMode] = useState<TotalMode>("purchasePrice");
  const [loading, setLoading] = useState(true);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [suggestions, setSuggestions] = useState({
    brands: [] as string[],
    models: [] as string[],
  });
  const [filters, setFilters] = useState(() => {
    const range = getCurrentMonthRange();
    return {
      startDate: range.startDate,
      endDate: range.endDate,
      brand: "",
      model: "",
      partnerId: "",
    };
  });

  const fetchReport = async () => {
    try {
      setLoading(true);
      const response = await api.get<ApiResponse<DistributedVehiclesReport>>(
        "/reports/distributed-vehicles",
        {
          params: {
            startDate: filters.startDate || undefined,
            endDate: filters.endDate || undefined,
            brand: filters.brand || undefined,
            model: filters.model || undefined,
            partnerId: filters.partnerId || undefined,
          },
        }
      );
      setReport(response.data.data);
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  const fetchPartners = async () => {
    try {
      const response = await api.get<ApiResponse<PartnerListResponse>>("/partners");
      setPartners(response.data.data.partners);
    } catch (error) {
      showToast(extractErrorMessage(error));
    }
  };

  const fetchSuggestions = async () => {
    try {
      const response = await fetchVehicleSuggestions();
      setSuggestions({
        brands: response.brands,
        models: response.models,
      });
    } catch (error) {
      showToast(extractErrorMessage(error));
    }
  };

  useEffect(() => {
    fetchReport();
    fetchPartners();
    fetchSuggestions();
  }, []);

  const calculatePartnerTotal = (partner: ReportPartnerGroup) => {
    if (mode === "purchasePrice") {
      return partner.partnerVehiclesTotalValue;
    }
    return partner.vehicles.reduce((total, vehicle) => total + vehicle.totalCost, 0);
  };

  const overallTotal = report
    ? report.partners.reduce(
        (total, partner) => total + calculatePartnerTotal(partner),
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
            onClick={() => setMode("purchasePrice")}
            className={`rounded-full px-3 py-1 ${
              mode === "purchasePrice"
                ? "bg-white shadow"
                : "text-slate-500"
            }`}
          >
            {t("reports.distributed.mode.purchasePrice")}
          </button>
          <button
            type="button"
            onClick={() => setMode("totalCost")}
            className={`rounded-full px-3 py-1 ${
              mode === "totalCost" ? "bg-white shadow" : "text-slate-500"
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
            value={filters.startDate}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, startDate: event.target.value }))
            }
          />
          <DateInput
            label={t("filters.endDate")}
            type="date"
            value={filters.endDate}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, endDate: event.target.value }))
            }
          />
          <ComboboxInput
            label={t("filters.brand")}
            placeholder={t("placeholders.brand")}
            value={filters.brand}
            suggestions={suggestions.brands}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, brand: event.target.value }))
            }
          />
          <ComboboxInput
            label={t("filters.model")}
            placeholder={t("placeholders.model")}
            value={filters.model}
            suggestions={suggestions.models}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, model: event.target.value }))
            }
          />
          <SelectInput
            label={t("filters.partner")}
            value={filters.partnerId}
            options={[
              { value: "", label: t("filters.allPartners") },
              ...partners.map((partner) => ({
                value: partner.id,
                label: partner.name,
              })),
            ]}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, partnerId: event.target.value }))
            }
          />
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          <button
            type="button"
            onClick={fetchReport}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
          >
            {t("actions.applyFilters")}
          </button>
          <button
            type="button"
            onClick={() => {
              const range = getCurrentMonthRange();
              setFilters({
                startDate: range.startDate,
                endDate: range.endDate,
                brand: "",
                model: "",
                partnerId: "",
              });
              setTimeout(fetchReport, 0);
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
              count: report?.overallVehiclesCount ?? 0,
            })}
          </div>
        </div>
      </div>

      {loading ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500 shadow-sm">
          {t("reports.distributed.loading")}
        </div>
      ) : null}

      {report?.partners.map((partner) => (
        <div
          key={partner.partnerId}
          className="rounded-lg border border-slate-200 bg-white shadow-sm"
        >
          <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
            <div>
              <h3 className="text-lg font-semibold">{partner.partnerName}</h3>
              <p className="text-sm text-slate-500">
                {t("reports.distributed.vehiclesCount", {
                  count: partner.partnerVehiclesCount,
                })}
              </p>
            </div>
            <div className="text-right">
              <div className="text-xs text-slate-500">
                {t("reports.distributed.partnerTotal")}
              </div>
              <div className="text-base font-semibold">
                {formatMoney(calculatePartnerTotal(partner))}
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
              {partner.vehicles.map((vehicle) => (
                <tr key={vehicle.vehicleId} className="border-t">
                  <td className="px-6 py-3">{vehicle.licensePlate}</td>
                  <td className="px-6 py-3">{vehicle.brand}</td>
                  <td className="px-6 py-3">{vehicle.model}</td>
                  <td className="px-6 py-3">{vehicle.year}</td>
                  <td className="px-6 py-3">
                    {formatDate(vehicle.distributedAt)}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(vehicle.purchasePrice)}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(vehicle.purchaseCommission)}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(vehicle.totalCost)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}

      {!loading && report?.partners.length === 0 ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500">
          {t("reports.distributed.empty")}
        </div>
      ) : null}
    </div>
  );
}
