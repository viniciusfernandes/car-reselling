import { useTranslation } from "react-i18next";
import { VehicleDetail } from "../../../service/types";

interface Props {
  vehicle: VehicleDetail;
  deleting: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

export default function VehicleDeleteModal({ vehicle, deleting, onClose, onConfirm }: Props) {
  const { t } = useTranslation();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="absolute inset-0 bg-black/40"
        onClick={() => !deleting && onClose()}
      />
      <div className="relative w-full max-w-md rounded-xl bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-slate-800">
          {t("vehicles.delete.confirmTitle")}
        </h3>
        <p className="mt-3 text-sm text-slate-600">
          {t("vehicles.delete.confirmMessage", { plate: vehicle.licensePlate })}
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            disabled={deleting}
            onClick={onClose}
            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
          >
            {t("actions.cancel")}
          </button>
          <button
            type="button"
            disabled={deleting}
            onClick={onConfirm}
            className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
          >
            {deleting ? t("vehicles.delete.deleting") : t("vehicles.delete.confirm")}
          </button>
        </div>
      </div>
    </div>
  );
}
