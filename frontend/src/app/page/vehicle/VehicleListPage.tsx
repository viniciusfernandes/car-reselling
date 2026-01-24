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

export default function VehicleListPage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<VehicleStatus | "">("");
  const [data, setData] = useState<VehicleListResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const params = useMemo(
    () => ({
      q: query || undefined,
      status: status || undefined,
      page: 0,
      size: 50,
    }),
    [query, status]
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
  }, [params.q, params.status]);

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
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search by plate, model, brand"
          className="w-full max-w-sm rounded-md border border-slate-200 px-3 py-2 text-sm shadow-sm"
        />
        <div className="flex flex-wrap gap-2">
          {STATUSES.map((option) => (
            <button
              key={option.value || "all"}
              type="button"
              onClick={() => setStatus(option.value)}
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
                <th className="px-4 py-3">Purchase Price</th>
                <th className="px-4 py-3">Services Total</th>
                <th className="px-4 py-3">Total Cost</th>
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
                    <td className="px-4 py-3">{vehicle.status}</td>
                    <td className="px-4 py-3">
                      {vehicle.purchasePrice.toFixed(2)}
                    </td>
                    <td className="px-4 py-3">
                      {vehicle.servicesTotal.toFixed(2)}
                    </td>
                    <td className="px-4 py-3">{vehicle.totalCost.toFixed(2)}</td>
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
    </div>
  );
}
