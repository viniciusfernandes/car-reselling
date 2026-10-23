import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage, extractFieldErrors } from "../../../service/api";
import {
  MarcaItem,
  CorItem,
  DocumentoItem,
  ParceiroItem,
  DetalheVeiculo,
  StatusVeiculo,
} from "../../../service/types";
import { useToast } from "../../../component/notification/ToastProvider";
import MoneyInput from "../../../component/input/MoneyInput";
import SelectInput from "../../../component/input/SelectInput";
import TextInput from "../../../component/input/TextInput";
import { formatNumber, parseMoney } from "../../../service/formatters";
import VeiculoFormFields, { VeiculoFormValues } from "./VeiculoFormFields";

interface Props {
  veiculoId: string;
  veiculo: DetalheVeiculo;
  parceiros: ParceiroItem[];
  marcaOptions: MarcaItem[];
  corOptions: CorItem[];
  documentos: DocumentoItem[];
  onRefresh: () => Promise<void>;
}

export default function VeiculoOverviewTab({
  veiculoId,
  veiculo,
  parceiros,
  marcaOptions,
  corOptions,
  onRefresh,
}: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [isUpdatingVeiculo, setIsUpdatingVeiculo] = useState(false);
  const [statusTarget, setStatusTarget] = useState<StatusVeiculo>(veiculo.status);
  const [parceiroId, setParceiroId] = useState(veiculo.parceiroAtribuidoId ?? "");
  const [precoVenda, setPrecoVenda] = useState(
    veiculo.precoVenda != null ? formatNumber(veiculo.precoVenda) : ""
  );
  const [formValues, setFormValues] = useState<VeiculoFormValues>({
    ano: veiculo.ano.toString(),
    cor: veiculo.cor,
    modelo: veiculo.modelo,
    marca: veiculo.marca,
    origemFornecedor: veiculo.origemFornecedor,
    precoCompra: formatNumber(veiculo.precoCompra),
    custoFrete: formatNumber(veiculo.custoFrete),
    comissaoCompra: formatNumber(veiculo.comissaoCompra ?? 0),
    valorFipe: veiculo.valorFipe != null ? formatNumber(veiculo.valorFipe) : "",
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  const statusOptions: Array<{ value: StatusVeiculo; label: string }> = [
    { value: "NO_PATIO", label: t("status.NO_PATIO") },
    { value: "PRONTO_PARA_DISTRIBUICAO", label: t("status.PRONTO_PARA_DISTRIBUICAO") },
    { value: "DISTRIBUIDO", label: t("status.DISTRIBUIDO") },
    { value: "VENDIDO", label: t("status.VENDIDO") },
  ];

  useEffect(() => {
    setFormValues({
      ano: veiculo.ano.toString(),
      cor: veiculo.cor,
      modelo: veiculo.modelo,
      marca: veiculo.marca,
      origemFornecedor: veiculo.origemFornecedor,
      precoCompra: formatNumber(veiculo.precoCompra),
      custoFrete: formatNumber(veiculo.custoFrete),
      comissaoCompra: formatNumber(veiculo.comissaoCompra ?? 0),
      valorFipe: veiculo.valorFipe != null ? formatNumber(veiculo.valorFipe) : "",
    });
    setParceiroId(veiculo.parceiroAtribuidoId ?? "");
    setStatusTarget(veiculo.status);
    setPrecoVenda(veiculo.precoVenda != null ? formatNumber(veiculo.precoVenda) : "");
  }, [veiculo]);

  const getMoneyError = (value: string, required = false) => {
    if (!value && required) return t("validation.required");
    if (!value) return "";
    const numeric = parseMoney(value);
    if (Number.isNaN(numeric) || numeric < 0) return t("validation.invalidValue");
    return "";
  };

  const handleFormChange = (field: keyof VeiculoFormValues, value: string) => {
    setFormValues((prev) => ({ ...prev, [field]: value }));
  };

  const handleFormBlur = (field: keyof VeiculoFormValues, currentValue: string) => {
    const nextErrors = { ...formErrors };
    let val = currentValue;

    if (field === "cor") {
      val = currentValue.trim().toUpperCase();
      setFormValues((prev) => ({ ...prev, cor: val }));
    }

    switch (field) {
      case "ano":
        if (!val) nextErrors.year = t("validation.required");
        else delete nextErrors.year;
        break;
      case "cor":
      case "modelo":
      case "marca":
        if (!val) nextErrors[field] = t("validation.required");
        else delete nextErrors[field];
        break;
      case "precoCompra": {
        const e = getMoneyError(val, true);
        if (e) nextErrors.purchasePrice = e;
        else delete nextErrors.purchasePrice;
        break;
      }
      case "custoFrete": {
        const e = getMoneyError(val);
        if (e) nextErrors.freightCost = e;
        else delete nextErrors.freightCost;
        break;
      }
      case "comissaoCompra": {
        const e = getMoneyError(val, true);
        if (e) nextErrors.purchaseCommission = e;
        else delete nextErrors.purchaseCommission;
        break;
      }
    }
    setFormErrors(nextErrors);
  };

  const validateForm = () => {
    const nextErrors: Record<string, string> = {};
    if (!formValues.ano) nextErrors.year = t("validation.required");
    if (!formValues.cor) nextErrors.color = t("validation.required");
    if (!formValues.modelo) nextErrors.model = t("validation.required");
    if (!formValues.marca) nextErrors.brand = t("validation.required");
    const compraError = getMoneyError(formValues.precoCompra, true);
    if (compraError) nextErrors.purchasePrice = compraError;
    const freteError = getMoneyError(formValues.custoFrete);
    if (freteError) nextErrors.freightCost = freteError;
    const comissaoError = getMoneyError(formValues.comissaoCompra, true);
    if (comissaoError) nextErrors.purchaseCommission = comissaoError;
    setFormErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleUpdateVeiculo = async () => {
    if (statusTarget === "DISTRIBUIDO" && !parceiroId) {
      showToast(t("vehicleDetail.selectPartnerFirst"), "error");
      return;
    }
    if (statusTarget === "VENDIDO") {
      const error = getMoneyError(precoVenda, true);
      if (error) {
        showToast(t("vehicleDetail.setSellingPriceFirst"), "error");
        return;
      }
    }
    if (!validateForm()) return;
    try {
      setIsUpdatingVeiculo(true);
      await api.put(`/veiculos/${veiculoId}`, {
        ano: Number(formValues.ano),
        cor: formValues.cor,
        modelo: formValues.modelo,
        marca: formValues.marca,
        origemFornecedor: formValues.origemFornecedor,
        precoCompra: parseMoney(formValues.precoCompra),
        custoFrete: parseMoney(formValues.custoFrete),
        comissaoCompra: parseMoney(formValues.comissaoCompra),
        valorFipe: formValues.valorFipe ? parseMoney(formValues.valorFipe) : null,
      });
      if ((statusTarget === "DISTRIBUIDO" || statusTarget === "VENDIDO") && precoVenda) {
        await api.put(`/veiculos/${veiculoId}/preco-venda`, {
          precoVenda: parseMoney(precoVenda),
        });
      }
      await api.post(`/veiculos/${veiculoId}/status`, {
        status: statusTarget,
        parceiroAtribuidoId:
          (statusTarget === "DISTRIBUIDO" || statusTarget === "VENDIDO") && parceiroId
            ? parceiroId
            : null,
      });
      showToast(t("vehicleDetail.updated"), "success");
      setFormErrors({});
      await onRefresh();
    } catch (error: any) {
      if (error?.response?.data?.errors) {
        setFormErrors(extractFieldErrors(error.response.data.errors));
      }
      showToast(extractErrorMessage(error), "error");
    } finally {
      setIsUpdatingVeiculo(false);
    }
  };

  return (
    <div className="space-y-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div className="grid gap-4 md:grid-cols-2">
        <TextInput label={t("vehicleDetail.licensePlate")} value={veiculo.placa} disabled />
        <SelectInput
          label={t("vehicleDetail.statusLabel")}
          value={statusTarget}
          options={statusOptions}
          disabled={veiculo.status === "VENDIDO"}
          onChange={(e) => setStatusTarget(e.target.value as StatusVeiculo)}
        />

        <VeiculoFormFields
          values={formValues}
          errors={formErrors}
          marcaOptions={marcaOptions}
          corOptions={corOptions}
          onChange={handleFormChange}
          onBlur={handleFormBlur}
        />

        {veiculo.status === "DISTRIBUIDO" ||
        veiculo.status === "VENDIDO" ||
        statusTarget === "VENDIDO" ? (
          <MoneyInput
            label={t("vehicleDetail.sellingPrice")}
            value={precoVenda}
            onValueChange={setPrecoVenda}
            required={veiculo.status === "DISTRIBUIDO" || statusTarget === "VENDIDO"}
            disabled={veiculo.status === "VENDIDO"}
          />
        ) : null}

        {statusTarget === "DISTRIBUIDO" || statusTarget === "VENDIDO" ? (
          <SelectInput
            label={t("vehicleDetail.partnerRequired")}
            value={parceiroId}
            options={[
              { value: "", label: t("vehicleDetail.selectPartner") },
              ...parceiros.map((p) => ({ value: p.id, label: p.nome })),
            ]}
            onChange={(e) => setParceiroId(e.target.value)}
          />
        ) : null}
      </div>

      <div className="flex justify-end">
        <button
          type="button"
          onClick={handleUpdateVeiculo}
          disabled={isUpdatingVeiculo}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {isUpdatingVeiculo ? t("actions.saving") : t("actions.saveChanges")}
        </button>
      </div>
    </div>
  );
}
