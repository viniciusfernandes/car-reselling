import { useTranslation } from "react-i18next";
import { DetalheVeiculo, ImpostosVeiculo } from "../../../service/types";
import { formatMoney } from "../../../service/formatters";

interface Props {
  veiculo: DetalheVeiculo;
  impostos: ImpostosVeiculo | null;
}

export default function ImpostosVeiculoTab({ veiculo, impostos }: Props) {
  const { t } = useTranslation();

  return (
    <div className="space-y-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h3 className="text-sm font-semibold text-slate-700">
          {t("vehicleDetail.taxes.title")}
        </h3>
        <p className="text-sm text-slate-500">{t("vehicleDetail.taxes.subtitle")}</p>
      </div>
      {veiculo.precoVenda != null ? (
        <div className="grid gap-4 md:grid-cols-3">
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.icms")}</div>
            <div className="text-base font-semibold">{formatMoney(impostos?.icms ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.pis")}</div>
            <div className="text-base font-semibold">{formatMoney(impostos?.pis ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.cofins")}</div>
            <div className="text-base font-semibold">{formatMoney(impostos?.cofins ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.csll")}</div>
            <div className="text-base font-semibold">{formatMoney(impostos?.csll ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.irpj")}</div>
            <div className="text-base font-semibold">{formatMoney(impostos?.irpj ?? 0)}</div>
          </div>
          <div className="rounded-md border border-slate-100 p-4">
            <div className="text-xs text-slate-500">{t("taxes.total")}</div>
            <div className="text-base font-semibold">{formatMoney(impostos?.totalImpostos ?? 0)}</div>
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
