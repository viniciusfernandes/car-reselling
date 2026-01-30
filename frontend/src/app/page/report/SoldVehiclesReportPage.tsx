import { useEffect, useState } from "react";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  PartnerItem,
  PartnerListResponse,
  SoldVehiclesReport,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import TextInput from "../../component/input/TextInput";
import SelectInput from "../../component/input/SelectInput";
import ComboboxInput from "../../component/input/ComboboxInput";
import { fetchVehicleSuggestions } from "../../service/vehicleSuggestions";

const formatMoney = (value: number) =>
  new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);

const getCurrentMonthRange = () => {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1);
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  const toDateInput = (value: Date) => value.toISOString().slice(0, 10);
  return { startDate: toDateInput(start), endDate: toDateInput(end) };
};

export default function SoldVehiclesReportPage() {
  const { showToast } = useToast();
  const [report, setReport] = useState<SoldVehiclesReport | null>(null);
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

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold">Sales Summary</h2>
          <p className="text-sm text-slate-500">
            Overview of sold vehicles and profitability.
          </p>
        </div>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="grid gap-4 md:grid-cols-5">
          <TextInput
            label="Start date"
            type="date"
            value={filters.startDate}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, startDate: event.target.value }))
            }
          />
          <TextInput
            label="End date"
            type="date"
            value={filters.endDate}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, endDate: event.target.value }))
            }
          />
          <ComboboxInput
            label="Brand"
            placeholder="e.g. Honda"
            value={filters.brand}
            suggestions={suggestions.brands}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, brand: event.target.value }))
            }
          />
          <ComboboxInput
            label="Model"
            placeholder="e.g. Civic"
            value={filters.model}
            suggestions={suggestions.models}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, model: event.target.value }))
            }
          />
          <SelectInput
            label="Partner"
            value={filters.partnerId}
            options={[
              { value: "", label: "All partners" },
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
            Apply filters
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
            Clear filters
          </button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-5">
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">Total vehicles sold</div>
          <div className="text-lg font-semibold">
            {report?.totalVehiclesSold ?? 0}
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">Total sold value</div>
          <div className="text-lg font-semibold">
            {formatMoney(report?.totalSoldValue ?? 0)}
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">Total taxes value</div>
          <div className="text-lg font-semibold">
            {formatMoney(report?.totalTaxesValue ?? 0)}
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">Total service value</div>
          <div className="text-lg font-semibold">
            {formatMoney(report?.totalServiceValue ?? 0)}
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="text-xs text-slate-500">Profit</div>
          <div className="text-lg font-semibold">
            {formatMoney(report?.profit ?? 0)}
          </div>
        </div>
      </div>

      {loading ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500 shadow-sm">
          Loading sales data...
        </div>
      ) : null}

      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-6 py-3">Plate</th>
              <th className="px-6 py-3">Brand</th>
              <th className="px-6 py-3">Model</th>
              <th className="px-6 py-3">Year</th>
              <th className="px-6 py-3 text-right">Selling Price</th>
              <th className="px-6 py-3 text-right">Total Taxes</th>
              <th className="px-6 py-3 text-right">Services Total</th>
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
                  No sold vehicles found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
