import { useTranslation } from "react-i18next";
import { VehicleDetail, VehicleStatus } from "../../../service/types";
import { formatMoney } from "../../../service/formatters";

const STATUS_KEYS: Record<VehicleStatus, string> = {
  IN_LOT: "status.IN_LOT",
  READY_FOR_DISTRIBUTION: "status.READY_FOR_DISTRIBUTION",
  DISTRIBUTED: "status.DISTRIBUTED",
  SOLD: "status.SOLD",
};

interface Props {
  vehicle: VehicleDetail;
  servicesTotal: number;
  onDeleteClick: () => void;
}

export default function VehicleDetailHeader({ vehicle, servicesTotal, onDeleteClick }: Props) {
  const { t } = useTranslation();

  return (
    <div className="flex items-center justify-between">
      <div>
        <h2 className="text-xl font-semibold">
          {vehicle.licensePlate} · {vehicle.brand} {vehicle.model}
        </h2>
        <p className="text-sm text-slate-500">
          {t("vehicleDetail.status", { status: t(STATUS_KEYS[vehicle.status]) })}
        </p>
      </div>
      <div className="flex items-center gap-4">
        <div className="text-right text-sm text-slate-500">
          <div>{t("vehicleDetail.servicesTotal", { value: formatMoney(servicesTotal) })}</div>
          <div>{t("vehicleDetail.totalCost", { value: formatMoney(vehicle.totalCost) })}</div>
        </div>
        <button
          type="button"
          onClick={onDeleteClick}
          className="rounded-md border border-red-200 px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
        >
          {t("vehicles.delete.button")}
        </button>
      </div>
    </div>
  );
}
