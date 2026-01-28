import { useEffect, useState } from "react";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  DistributedVehiclesReport,
  PartnerItem,
  PartnerListResponse,
  ReportPartnerGroup,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import TextInput from "../../component/input/TextInput";
import SelectInput from "../../component/input/SelectInput";

type TotalMode = "purchasePrice" | "totalCost";

export default function DistributedVehiclesReportPage() {
  const { showToast } = useToast();
  const [report, setReport] = useState<DistributedVehiclesReport | null>(null);
  const [mode, setMode] = useState<TotalMode>("purchasePrice");
  const [loading, setLoading] = useState(true);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [filters, setFilters] = useState({
    startDate: "",
    endDate: "",
    brand: "",
    model: "",
    partnerId: "",
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

  useEffect(() => {
    fetchReport();
    fetchPartners();
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

  const formatMoney = (value: number) =>
    new Intl.NumberFormat("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold">Distributed Vehicles</h2>
          <p className="text-sm text-slate-500">
            Totals grouped by partner dealership.
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
            Purchase Price
          </button>
          <button
            type="button"
            onClick={() => setMode("totalCost")}
            className={`rounded-full px-3 py-1 ${
              mode === "totalCost" ? "bg-white shadow" : "text-slate-500"
            }`}
          >
            Total Cost
          </button>
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
          <TextInput
            label="Brand"
            placeholder="e.g. Honda"
            value={filters.brand}
            onChange={(event) =>
              setFilters((prev) => ({ ...prev, brand: event.target.value }))
            }
          />
          <TextInput
            label="Model"
            placeholder="e.g. Civic"
            value={filters.model}
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
              setFilters({
                startDate: "",
                endDate: "",
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

      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-sm text-slate-500">Overall total</div>
            <div className="text-xl font-semibold">
              {formatMoney(overallTotal)}
            </div>
          </div>
          <div className="text-sm text-slate-500">
            {report?.overallVehiclesCount ?? 0} vehicles
          </div>
        </div>
      </div>

      {loading ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500 shadow-sm">
          Loading report...
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
                {partner.partnerVehiclesCount} vehicles
              </p>
            </div>
            <div className="text-right">
              <div className="text-xs text-slate-500">Partner total</div>
              <div className="text-base font-semibold">
                {formatMoney(calculatePartnerTotal(partner))}
              </div>
            </div>
          </div>
          <table className="min-w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-6 py-3">Plate</th>
                <th className="px-6 py-3">Brand</th>
                <th className="px-6 py-3">Model</th>
                <th className="px-6 py-3">Year</th>
                <th className="px-6 py-3">Distributed Date</th>
                <th className="px-6 py-3 text-right">Purchase Price</th>
                <th className="px-6 py-3 text-right">Total Cost</th>
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
                    {vehicle.distributedAt
                      ? new Date(vehicle.distributedAt).toLocaleDateString()
                      : "-"}
                  </td>
                  <td className="px-6 py-3 text-right">
                    {formatMoney(vehicle.purchasePrice)}
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
          No distributed vehicles yet.
        </div>
      ) : null}
    </div>
  );
}
