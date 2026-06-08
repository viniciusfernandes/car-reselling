import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { extractErrorMessage } from "../../../service/api";
import { fetchModelsByBrand } from "../../../service/brandModels";
import { BrandItem, ColorItem, ModelItem, SupplierSource } from "../../../service/types";
import ComboboxInput from "../../../component/input/ComboboxInput";
import MoneyInput from "../../../component/input/MoneyInput";
import NumberInput from "../../../component/input/NumberInput";
import SelectInput from "../../../component/input/SelectInput";
import { useToast } from "../../../component/notification/ToastProvider";

export type VehicleFormValues = {
  year: string;
  color: string;
  model: string;
  brand: string;
  supplierSource: SupplierSource;
  purchasePrice: string;
  freightCost: string;
  purchaseCommission: string;
  valorFipe: string;
};

interface Props {
  values: VehicleFormValues;
  errors: Record<string, string>;
  brandOptions: BrandItem[];
  colorOptions: ColorItem[];
  onChange: (field: keyof VehicleFormValues, value: string) => void;
  onBlur?: (field: keyof VehicleFormValues, currentValue: string) => void;
}

const SUPPLIER_OPTIONS: { value: SupplierSource; labelKey: string }[] = [
  { value: "INTERNET", labelKey: "supplier.internet" },
  { value: "PERSONAL_CONTACT", labelKey: "supplier.personalContact" },
];

export default function VehicleFormFields({
  values,
  errors,
  brandOptions,
  colorOptions,
  onChange,
  onBlur,
}: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [modelOptions, setModelOptions] = useState<ModelItem[]>([]);

  // Ref to read current model without including it in effect deps,
  // preventing unnecessary model clears on unrelated re-renders.
  const currentModelRef = useRef(values.model);
  currentModelRef.current = values.model;

  // Stable ref for onChange to avoid re-triggering the effect when
  // the parent re-creates the callback on each render.
  const onChangeRef = useRef(onChange);
  onChangeRef.current = onChange;

  useEffect(() => {
    const selectedBrand = brandOptions.find((b) => b.name === values.brand);
    if (!selectedBrand) {
      setModelOptions([]);
      return;
    }
    let cancelled = false;
    fetchModelsByBrand(selectedBrand.id)
      .then((models) => {
        if (cancelled) return;
        setModelOptions(models);
        if (currentModelRef.current && !models.some((m) => m.name === currentModelRef.current)) {
          onChangeRef.current("model", "");
        }
      })
      .catch((error) => {
        if (!cancelled) showToast(extractErrorMessage(error), "error");
      });
    return () => {
      cancelled = true;
    };
  }, [brandOptions, values.brand, showToast]);

  const handleBlur = (field: keyof VehicleFormValues) => {
    onBlur?.(field, values[field]);
  };

  return (
    <>
      <SelectInput
        label={t("vehicleDetail.supplierSource")}
        value={values.supplierSource}
        options={SUPPLIER_OPTIONS.map((opt) => ({
          value: opt.value,
          label: t(opt.labelKey),
        }))}
        required
        onChange={(e) => onChange("supplierSource", e.target.value)}
      />
      <NumberInput
        label={t("vehicleDetail.year")}
        value={values.year}
        required
        min={1900}
        max={new Date().getFullYear() + 1}
        onChange={(e) => onChange("year", e.target.value)}
        onBlur={() => handleBlur("year")}
        error={errors.year}
      />
      <ComboboxInput
        label={t("vehicleDetail.brand")}
        value={values.brand}
        required
        suggestions={brandOptions.map((b) => b.name)}
        onChange={(e) => onChange("brand", e.target.value)}
        onBlur={() => handleBlur("brand")}
        error={errors.brand}
      />
      <ComboboxInput
        label={t("vehicleDetail.model")}
        value={values.model}
        required
        suggestions={modelOptions.map((m) => m.name)}
        onChange={(e) => onChange("model", e.target.value)}
        onBlur={() => handleBlur("model")}
        error={errors.model}
      />
      <ComboboxInput
        label={t("vehicleDetail.color")}
        value={values.color}
        required
        suggestions={colorOptions.map((c) => c.name)}
        onChange={(e) => onChange("color", e.target.value)}
        onBlur={() => handleBlur("color")}
        error={errors.color}
      />
      <MoneyInput
        label={t("vehicleDetail.purchasePrice")}
        value={values.purchasePrice}
        required
        onValueChange={(value) => onChange("purchasePrice", value)}
        onBlur={() => handleBlur("purchasePrice")}
        error={errors.purchasePrice}
      />
      <MoneyInput
        label={t("vehicleDetail.purchaseCommission")}
        value={values.purchaseCommission}
        required
        onValueChange={(value) => onChange("purchaseCommission", value)}
        onBlur={() => handleBlur("purchaseCommission")}
        error={errors.purchaseCommission}
      />
      <MoneyInput
        label={t("vehicleDetail.freightCost")}
        value={values.freightCost}
        onValueChange={(value) => onChange("freightCost", value)}
        onBlur={() => handleBlur("freightCost")}
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
