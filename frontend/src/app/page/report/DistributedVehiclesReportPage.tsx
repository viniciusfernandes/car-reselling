import { useEffect, useState } from "react";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  DistributedVehiclesReport,
  ReportPartnerGroup,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";

type TotalMode = "purchasePrice" | "totalCost";

export default function DistributedVehiclesReportPage() {
  const { showToast } = useToast();
  const [report, setReport] = useState<DistributedVehiclesReport | null>(null);
  const [mode, setMode] = useState<TotalMode>("purchasePrice");

  const fetchReport = async () => {
    try {
      const response = await api.get<ApiResponse<DistributedVehiclesReport>>(
        "/reports/distributed-vehicles"
      );
      setReport(response.data.data);
    } catch (error) {
      showToast(extractErrorMessage(error));
    }
  };

  useEffect(() => {
    fetchReport();
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
        <div className="flex items-center justify-between">
          <div>
            <div className="text-sm text-slate-500">Overall total</div>
            <div className="text-xl font-semibold">{overallTotal.toFixed(2)}</div>
          </div>
          <div className="text-sm text-slate-500">
            {report?.overallVehiclesCount ?? 0} vehicles
          </div>
        </div>
      </div>

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
                {calculatePartnerTotal(partner).toFixed(2)}
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
                <th className="px-6 py-3">Purchase Price</th>
                <th className="px-6 py-3">Total Cost</th>
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
                    {vehicle.purchasePrice.toFixed(2)}
                  </td>
                  <td className="px-6 py-3">{vehicle.totalCost.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}

      {report?.partners.length === 0 ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500">
          No distributed vehicles yet.
        </div>
      ) : null}
    </div>
  );
}
