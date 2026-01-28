import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  VehicleListResponse,
  VehicleStatus,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";

const STATUSES: Array<{ value: VehicleStatus | ""; label: string }> = [
  { value: "", label: "All" },
  { value: "IN_LOT", label: "In lot" },
  { value: "IN_SERVICE", label: "In service" },
  { value: "READY_FOR_DISTRIBUTION", label: "Ready" },
  { value: "DISTRIBUTED", label: "Distributed" },
];

const STATUS_LABELS: Record<VehicleStatus, string> = {
  IN_LOT: "In lot",
  IN_SERVICE: "In service",
  READY_FOR_DISTRIBUTION: "Ready",
  DISTRIBUTED: "Distributed",
};

const formatMoney = (value: number) =>
  new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);

export default function VehicleListPage() {
  const navigate = useNavigate();
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

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold">Vehicles</h2>
          <p className="text-sm text-slate-500">
            Track all vehicles in the lot and their totals.
          </p>
        </div>
        <button
          type="button"
          onClick={() => navigate("/vehicles/new")}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white shadow"
        >
          New Vehicle
        </button>
      </div>
      <div className="flex flex-wrap gap-3">
        <input
          value={query}
          onChange={(event) => {
            setQuery(event.target.value);
            setPage(0);
          }}
          placeholder="Search by plate, model, brand"
          className="w-full max-w-sm rounded-md border border-slate-200 px-3 py-2 text-sm shadow-sm"
        />
        <div className="flex flex-wrap gap-2">
          {STATUSES.map((option) => (
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
                <th className="px-4 py-3">Plate</th>
                <th className="px-4 py-3">Brand</th>
                <th className="px-4 py-3">Model</th>
                <th className="px-4 py-3">Year</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3 text-right">Purchase Price</th>
                <th className="px-4 py-3 text-right">Services Total</th>
                <th className="px-4 py-3 text-right">Total Cost</th>
                <th className="px-4 py-3">Assigned Partner</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={9} className="px-4 py-6 text-center">
                    Loading vehicles...
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
                      {STATUS_LABELS[vehicle.status]}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {formatMoney(vehicle.purchasePrice)}
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
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={9} className="px-4 py-6 text-center text-slate-500">
                    No vehicles found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
      <div className="flex items-center justify-between text-sm text-slate-500">
        <span>
          Page {page + 1} of {totalPages || 1}
        </span>
        <div className="flex gap-2">
          <button
            type="button"
            disabled={!canGoPrev}
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            className="rounded-md border border-slate-200 px-3 py-1 disabled:cursor-not-allowed disabled:text-slate-300"
          >
            Previous
          </button>
          <button
            type="button"
            disabled={!canGoNext}
            onClick={() => setPage((prev) => prev + 1)}
            className="rounded-md border border-slate-200 px-3 py-1 disabled:cursor-not-allowed disabled:text-slate-300"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
