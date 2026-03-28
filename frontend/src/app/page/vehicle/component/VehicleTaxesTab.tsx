import { useTranslation } from "react-i18next";
import { VehicleDetail, VehicleTaxes } from "../../../service/types";
import { formatMoney } from "../../../service/formatters";

interface Props {
  vehicle: VehicleDetail;
  taxes: VehicleTaxes | null;
}

export default function VehicleTaxesTab({ vehicle, taxes }: Props) {
  const { t } = useTranslation();

  return (
    <div className="space-y-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h3 className="text-sm font-semibold text-slate-700">
          {t("vehicleDetail.taxes.title")}
        </h3>
        <p className="text-sm text-slate-500">{t("vehicleDetail.taxes.subtitle")}</p>
      </div>
      {vehicle.sellingPrice != null ? (
        <div className="grid gap-4 md:grid-cols-3">
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.icms")}</div>
            <div className="text-base font-semibold">{formatMoney(taxes?.icms ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.pis")}</div>
            <div className="text-base font-semibold">{formatMoney(taxes?.pis ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.cofins")}</div>
            <div className="text-base font-semibold">{formatMoney(taxes?.cofins ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.csll")}</div>
            <div className="text-base font-semibold">{formatMoney(taxes?.csll ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.irpj")}</div>
            <div className="text-base font-semibold">{formatMoney(taxes?.irpj ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.total")}</div>
            <div className="text-base font-semibold">{formatMoney(taxes?.totalTaxes ?? 0)}</div>
          </div>
        </div>
      ) : (
        <div className="rounded-md border border-dashed border-slate-200 p-4 text-sm text-slate-500">
          {t("vehicleDetail.taxes.missingSellingPrice")}
        </div>
      )}
    </div>
  );
}
