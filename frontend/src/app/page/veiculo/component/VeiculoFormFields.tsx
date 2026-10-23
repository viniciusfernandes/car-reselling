import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { extractErrorMessage } from "../../../service/api";
import { fetchModelosByMarca } from "../../../service/marcaModelos";
import { MarcaItem, CorItem, ModeloItem, OrigemFornecedor } from "../../../service/types";
import ComboboxInput from "../../../component/input/ComboboxInput";
import MoneyInput from "../../../component/input/MoneyInput";
import NumberInput from "../../../component/input/NumberInput";
import SelectInput from "../../../component/input/SelectInput";
import { useToast } from "../../../component/notification/ToastProvider";

export type VeiculoFormValues = {
  ano: string;
  cor: string;
  modelo: string;
  marca: string;
  origemFornecedor: OrigemFornecedor;
  precoCompra: string;
  custoFrete: string;
  comissaoCompra: string;
  valorFipe: string;
};

interface Props {
  values: VeiculoFormValues;
  errors: Record<string, string>;
  marcaOptions: MarcaItem[];
  corOptions: CorItem[];
  onChange: (field: keyof VeiculoFormValues, value: string) => void;
  onBlur?: (field: keyof VeiculoFormValues, currentValue: string) => void;
}

const FORNECEDOR_OPTIONS: { value: OrigemFornecedor; labelKey: string }[] = [
  { value: "INTERNET", labelKey: "supplier.internet" },
  { value: "CONTATO_PESSOAL", labelKey: "supplier.personalContact" },
];

export default function VeiculoFormFields({
  values,
  errors,
  marcaOptions,
  corOptions,
  onChange,
  onBlur,
}: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [modeloOptions, setModeloOptions] = useState<ModeloItem[]>([]);

  // Ref to read current model without including it in effect deps,
  // preventing unnecessary model clears on unrelated re-renders.
  const currentModelRef = useRef(values.modelo);
  currentModelRef.current = values.modelo;

  // Stable ref for onChange to avoid re-triggering the effect when
  // the parent re-creates the callback on each render.
  const onChangeRef = useRef(onChange);
  onChangeRef.current = onChange;

  useEffect(() => {
    const selectedMarca = marcaOptions.find((b) => b.nome === values.marca);
    if (!selectedMarca) {
      setModeloOptions([]);
      return;
    }
    let cancelled = false;
    fetchModelosByMarca(selectedMarca.id)
      .then((modelos) => {
        if (cancelled) return;
        setModeloOptions(modelos);
        if (currentModelRef.current && !modelos.some((m) => m.nome === currentModelRef.current)) {
          onChangeRef.current("modelo", "");
        }
      })
      .catch((error) => {
        if (!cancelled) showToast(extractErrorMessage(error), "error");
      });
    return () => {
      cancelled = true;
    };
  }, [marcaOptions, values.marca, showToast]);

  const handleBlur = (field: keyof VeiculoFormValues) => {
    onBlur?.(field, values[field]);
  };

  return (
    <>
      <SelectInput
        label={t("vehicleDetail.supplierSource")}
        value={values.origemFornecedor}
        options={FORNECEDOR_OPTIONS.map((opt) => ({
          value: opt.value,
          label: t(opt.labelKey),
        }))}
        required
        onChange={(e) => onChange("origemFornecedor", e.target.value)}
      />
      <NumberInput
        label={t("vehicleDetail.year")}
        value={values.ano}
        required
        min={1900}
        max={new Date().getFullYear() + 1}
        onChange={(e) => onChange("ano", e.target.value)}
        onBlur={() => handleBlur("ano")}
        error={errors.year}
      />
      <ComboboxInput
        label={t("vehicleDetail.brand")}
        value={values.marca}
        required
        suggestions={marcaOptions.map((b) => b.nome)}
        onChange={(e) => onChange("marca", e.target.value)}
        onBlur={() => handleBlur("marca")}
        error={errors.brand}
      />
      <ComboboxInput
        label={t("vehicleDetail.model")}
        value={values.modelo}
        required
        suggestions={modeloOptions.map((m) => m.nome)}
        onChange={(e) => onChange("modelo", e.target.value)}
        onBlur={() => handleBlur("modelo")}
        error={errors.model}
      />
      <ComboboxInput
        label={t("vehicleDetail.color")}
        value={values.cor}
        required
        suggestions={corOptions.map((c) => c.nome)}
        onChange={(e) => onChange("cor", e.target.value)}
        onBlur={() => handleBlur("cor")}
        error={errors.color}
      />
      <MoneyInput
        label={t("vehicleDetail.purchasePrice")}
        value={values.precoCompra}
        required
        onValueChange={(value) => onChange("precoCompra", value)}
        onBlur={() => handleBlur("precoCompra")}
        error={errors.purchasePrice}
      />
      <MoneyInput
        label={t("vehicleDetail.purchaseCommission")}
        value={values.comissaoCompra}
        required
        onValueChange={(value) => onChange("comissaoCompra", value)}
        onBlur={() => handleBlur("comissaoCompra")}
        error={errors.purchaseCommission}
      />
      <MoneyInput
        label={t("vehicleDetail.freightCost")}
        value={values.custoFrete}
        onValueChange={(value) => onChange("custoFrete", value)}
        onBlur={() => handleBlur("custoFrete")}
        error={errors.freightCost}
      />
      <MoneyInput
        label={t("vehicleDetail.valorFipe")}
        value={values.valorFipe}
        onValueChange={(value) => onChange("valorFipe", value)}
      />
    </>
  );
}
