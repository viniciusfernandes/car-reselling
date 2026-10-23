import { useTranslation } from "react-i18next";
import { DetalheVeiculo, StatusVeiculo } from "../../../service/types";
import { formatMoney } from "../../../service/formatters";

const STATUS_KEYS: Record<StatusVeiculo, string> = {
  NO_PATIO: "status.NO_PATIO",
  PRONTO_PARA_DISTRIBUICAO: "status.PRONTO_PARA_DISTRIBUICAO",
  DISTRIBUIDO: "status.DISTRIBUIDO",
  VENDIDO: "status.VENDIDO",
};

interface Props {
  veiculo: DetalheVeiculo;
  totalServicos: number;
  onDeleteClick: () => void;
}

export default function DetalheVeiculoHeader({ veiculo, totalServicos, onDeleteClick }: Props) {
  const { t } = useTranslation();

  return (
    <div className="flex items-center justify-between">
      <div>
        <h2 className="text-xl font-semibold">
          {veiculo.placa} · {veiculo.marca} {veiculo.modelo}
        </h2>
        <p className="text-sm text-slate-500">
          {t("vehicleDetail.status", { status: t(STATUS_KEYS[veiculo.status]) })}
        </p>
      </div>
      <div className="flex items-center gap-4">
        <div className="text-right text-sm text-slate-500">
          <div>{t("vehicleDetail.servicesTotal", { value: formatMoney(totalServicos) })}</div>
          <div>{t("vehicleDetail.totalCost", { value: formatMoney(veiculo.custoTotal) })}</div>
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
