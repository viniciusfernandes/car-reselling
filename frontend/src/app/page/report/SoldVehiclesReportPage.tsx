import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  PartnerItem,
  PartnerListResponse,
  SoldVehiclesReport,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import DateInput from "../../component/input/DateInput";
import SelectInput from "../../component/input/SelectInput";
import ComboboxInput from "../../component/input/ComboboxInput";
import { fetchVehicleSuggestions } from "../../service/vehicleSuggestions";
import { formatMoney } from "../../service/formatters";

const getCurrentMonthRange = () => {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1);
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  const toDateInput = (value: Date) => value.toISOString().slice(0, 10);
  return { startDate: toDateInput(start), endDate: toDateInput(end) };
};

const toMonthYear = (date: Date) => {
  const month = String(date.getMonth() + 1).padStart(2, "0");
  return `${month}/${date.getFullYear()}`;
};

const getLast12MonthsRange = () => {
  const now = new Date();
  const end = new Date(now.getFullYear(), now.getMonth(), 1);
  const start = new Date(now.getFullYear(), now.getMonth() - 11, 1);
  return { startMonthYear: toMonthYear(start), endMonthYear: toMonthYear(end) };
};

const parseMonthYear = (value: string) => {
  const match = value.match(/^(\d{2})\/(\d{4})$/);
  if (!match) {
    return null;
  }
  const month = Number(match[1]);
  const year = Number(match[2]);
  if (month < 1 || month > 12) {
    return null;
  }
  return new Date(year, month - 1, 1);
};

const monthValueToMonthYear = (value: string) => {
  const match = value.match(/^(\d{4})-(\d{2})$/);
  if (!match) {
    return "";
  }
  return `${match[2]}/${match[1]}`;
};

const monthYearToMonthValue = (value: string) => {
  const parsed = parseMonthYear(value);
  if (!parsed) {
    return "";
  }
  const month = String(parsed.getMonth() + 1).padStart(2, "0");
  return `${parsed.getFullYear()}-${month}`;
};

const dateValueToMonthYear = (value: string) => {
  const match = value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!match) {
    return "";
  }
  return `${match[2]}/${match[1]}`;
};

const monthYearToDateValue = (value: string) => {
  const parsed = parseMonthYear(value);
  if (!parsed) {
    return "";
  }
  const month = String(parsed.getMonth() + 1).padStart(2, "0");
  return `${parsed.getFullYear()}-${month}-01`;
};

const clampToTwelveMonths = (start: Date, end: Date) => {
  const normalizedStart = new Date(start.getFullYear(), start.getMonth(), 1);
  const normalizedEnd = new Date(end.getFullYear(), end.getMonth(), 1);
  if (normalizedStart > normalizedEnd) {
    return { start: normalizedStart, end: normalizedStart };
  }
  const maxEnd = new Date(normalizedStart.getFullYear(), normalizedStart.getMonth() + 11, 1);
  if (normalizedEnd > maxEnd) {
    return { start: normalizedStart, end: maxEnd };
  }
  return { start: normalizedStart, end: normalizedEnd };
};

const getMonthLabels = (start: Date, end: Date) => {
  const labels: string[] = [];
  const cursor = new Date(start.getFullYear(), start.getMonth(), 1);
  while (cursor <= end) {
    labels.push(toMonthYear(cursor));
    cursor.setMonth(cursor.getMonth() + 1);
  }
  return labels;
};

export default function SoldVehiclesReportPage() {
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [report, setReport] = useState<SoldVehiclesReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [isHistogramOpen, setIsHistogramOpen] = useState(true);
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
  const [chartFilters, setChartFilters] = useState(() => getLast12MonthsRange());

  const fetchReport = async () => {
    try {
      setLoading(true);
      const response = await api.get<ApiResponse<SoldVehiclesReport>>(
        "/reports/sold-vehicles",
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

  const histogram = (() => {
    if (!report?.vehicles?.length) {
      return { labels: [] as string[], values: [] as number[] };
    }
    const start = parseMonthYear(chartFilters.startMonthYear);
    const end = parseMonthYear(chartFilters.endMonthYear);
    if (!start || !end || start > end) {
      return { labels: [] as string[], values: [] as number[] };
    }
    const labels = getMonthLabels(start, end);
    const totals = labels.reduce<Record<string, number>>((acc, label) => {
      acc[label] = 0;
      return acc;
    }, {});
    for (const vehicle of report.vehicles) {
      if (!vehicle.soldAt) {
        continue;
      }
      const date = new Date(vehicle.soldAt);
      if (Number.isNaN(date.getTime())) {
        continue;
      }
      const label = toMonthYear(new Date(date.getFullYear(), date.getMonth(), 1));
      if (label in totals) {
        totals[label] += vehicle.sellingPrice;
      }
    }
    return {
      labels,
      values: labels.map((label) => totals[label] ?? 0),
    };
  })();

  const maxHistogramValue =
    histogram.values.length > 0 ? Math.max(...histogram.values) : 0;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold">{t("reports.sales.title")}</h2>
          <p className="text-sm text-slate-500">
            {t("reports.sales.subtitle")}
          </p>
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

      <div className="grid gap-4 md:grid-cols-5">
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">
            {t("reports.sales.totals.vehicles")}
          </div>
          <div className="text-lg font-semibold">
            {report?.totalVehiclesSold ?? 0}
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">
            {t("reports.sales.totals.soldValue")}
          </div>
          <div className="text-lg font-semibold">
            {formatMoney(report?.totalSoldValue ?? 0)}
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">
            {t("reports.sales.totals.taxes")}
          </div>
          <div className="text-lg font-semibold">
            {formatMoney(report?.totalTaxesValue ?? 0)}
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">
            {t("reports.sales.totals.services")}
          </div>
          <div className="text-lg font-semibold">
            {formatMoney(report?.totalServiceValue ?? 0)}
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">{t("reports.sales.totals.profit")}</div>
          <div className="text-lg font-semibold">
            {formatMoney(report?.profit ?? 0)}
          </div>
        </div>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h3 className="text-base font-semibold">
              {t("reports.sales.histogram.title")}
            </h3>
            <p className="text-sm text-slate-500">
              {t("reports.sales.histogram.subtitle")}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setIsHistogramOpen((prev) => !prev)}
              className="rounded-md border border-slate-200 px-3 py-2 text-sm"
              aria-expanded={isHistogramOpen}
            >
              {isHistogramOpen
                ? t("reports.sales.histogram.hide")
                : t("reports.sales.histogram.show")}
            </button>
          </div>
        </div>

        {isHistogramOpen ? (
          <>
            <div className="mt-4 grid gap-3 sm:grid-cols-2">
              <DateInput
                label={t("reports.sales.histogram.startMonth")}
                type="date"
                value={monthYearToDateValue(chartFilters.startMonthYear)}
                onChange={(event) =>
                  setChartFilters((prev) => {
                    const start = parseMonthYear(dateValueToMonthYear(event.target.value));
                    const end = parseMonthYear(prev.endMonthYear);
                    if (!start || !end) {
                      return prev;
                    }
                    const clamped = clampToTwelveMonths(start, end);
                    return {
                      ...prev,
                      startMonthYear: toMonthYear(clamped.start),
                      endMonthYear: toMonthYear(clamped.end),
                    };
                  })
                }
              />
              <DateInput
                label={t("reports.sales.histogram.endMonth")}
                type="date"
                value={monthYearToDateValue(chartFilters.endMonthYear)}
                onChange={(event) =>
                  setChartFilters((prev) => {
                    const start = parseMonthYear(prev.startMonthYear);
                    const end = parseMonthYear(dateValueToMonthYear(event.target.value));
                    if (!start || !end) {
                      return prev;
                    }
                    const clamped = clampToTwelveMonths(start, end);
                    return {
                      ...prev,
                      startMonthYear: toMonthYear(clamped.start),
                      endMonthYear: toMonthYear(clamped.end),
                    };
                  })
                }
              />
            </div>

            {histogram.labels.length === 0 ? (
              <div className="mt-6 rounded-md border border-dashed border-slate-200 p-6 text-center text-sm text-slate-500">
                {t("reports.sales.histogram.empty")}
              </div>
            ) : (
              <div className="mt-6">
                <div className="flex items-end gap-2">
                  {histogram.labels.map((label, index) => {
                    const value = histogram.values[index] ?? 0;
                    const height =
                      maxHistogramValue > 0
                        ? Math.round((value / maxHistogramValue) * 160)
                        : 0;
                    return (
                      <div key={label} className="flex flex-1 flex-col items-center gap-2">
                        <div
                          className="w-full rounded-md bg-blue-900"
                          style={{ height: `${Math.max(height, 4)}px` }}
                          title={`${formatMoney(value)}`}
                        />
                        <div className="text-[10px] text-slate-500">{label}</div>
                      </div>
                    );
                  })}
                </div>
                <div className="mt-3 text-xs text-slate-500">
                  {t("reports.sales.histogram.maxMonth", {
                    value: formatMoney(maxHistogramValue),
                  })}
                </div>
              </div>
            )}
          </>
        ) : null}
      </div>

      {loading ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500 shadow-sm">
          {t("reports.sales.loading")}
        </div>
      ) : null}

      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-6 py-3">{t("reports.sales.table.plate")}</th>
              <th className="px-6 py-3">{t("reports.sales.table.brand")}</th>
              <th className="px-6 py-3">{t("reports.sales.table.model")}</th>
              <th className="px-6 py-3">{t("reports.sales.table.year")}</th>
              <th className="px-6 py-3 text-right">
                {t("reports.sales.table.sellingPrice")}
              </th>
              <th className="px-6 py-3 text-right">
                {t("reports.sales.table.totalTaxes")}
              </th>
              <th className="px-6 py-3 text-right">
                {t("reports.sales.table.servicesTotal")}
              </th>
            </tr>
          </thead>
          <tbody>
            {report?.vehicles.length ? (
              report.vehicles.map((vehicle) => (
                <tr key={vehicle.vehicleId} className="border-t">
                  <td className="px-6 py-3">{vehicle.licensePlate}</td>
                  <td className="px-6 py-3">{vehicle.brand}</td>
                  <td className="px-6 py-3">{vehicle.model}</td>
                  <td className="px-6 py-3">{vehicle.year}</td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(vehicle.sellingPrice)}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(vehicle.totalTaxes)}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(vehicle.servicesTotal)}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={7} className="px-6 py-6 text-center text-slate-500">
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
