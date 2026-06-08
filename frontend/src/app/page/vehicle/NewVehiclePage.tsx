import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { api, extractErrorMessage, extractFieldErrors } from "../../service/api";
import { ApiResponse, BrandItem, ColorItem } from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { fetchBrands, fetchColors } from "../../service/brandModels";
import { parseMoney } from "../../service/formatters";
import TextInput from "../../component/input/TextInput";
import VehicleFormFields, { VehicleFormValues } from "./component/VehicleFormFields";

const PLATE_REGEX = /^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$/;

const EMPTY_FORM_VALUES: VehicleFormValues = {
  year: new Date().getFullYear().toString(),
  color: "",
  model: "",
  brand: "",
  supplierSource: "INTERNET",
  purchasePrice: "",
  freightCost: "0,00",
  purchaseCommission: "0,00",
  valorFipe: "",
};

export default function NewVehiclePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [colorOptions, setColorOptions] = useState<ColorItem[]>([]);
  const [brandOptions, setBrandOptions] = useState<BrandItem[]>([]);

  const [licensePlate, setLicensePlate] = useState("");
  const [renavam, setRenavam] = useState("");
  const [vin, setVin] = useState("");
  const [formValues, setFormValues] = useState<VehicleFormValues>(EMPTY_FORM_VALUES);

  useEffect(() => {
    const loadLookupData = async () => {
      try {
        const [brands, colors] = await Promise.all([fetchBrands(), fetchColors()]);
        setBrandOptions(brands);
        setColorOptions(colors);
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    loadLookupData();
  }, [showToast]);

  const getMoneyError = (value: string, required = false) => {
    if (!value && required) return t("validation.required");
    if (!value) return "";
    const numeric = parseMoney(value);
    if (Number.isNaN(numeric) || numeric < 0) return t("validation.invalidValue");
    return "";
  };

  const handleFormChange = (field: keyof VehicleFormValues, value: string) => {
    setFormValues((prev) => ({ ...prev, [field]: value }));
  };

  const handleFormBlur = (field: keyof VehicleFormValues, currentValue: string) => {
    const nextErrors = { ...errors };
    let val = currentValue;

    if (field === "brand" || field === "model" || field === "color") {
      val = currentValue.trim().toUpperCase();
      setFormValues((prev) => ({ ...prev, [field]: val }));
    }

    switch (field) {
      case "year":
        if (!val) nextErrors.year = t("validation.required");
        else delete nextErrors.year;
        break;
      case "color":
      case "model":
      case "brand":
        if (!val) nextErrors[field] = t("validation.required");
        else delete nextErrors[field];
        break;
      case "purchasePrice": {
        const e = getMoneyError(val, true);
        if (e) nextErrors.purchasePrice = e;
        else delete nextErrors.purchasePrice;
        break;
      }
      case "freightCost": {
        const e = getMoneyError(val);
        if (e) nextErrors.freightCost = e;
        else delete nextErrors.freightCost;
        break;
      }
      case "purchaseCommission": {
        const e = getMoneyError(val);
        if (e) nextErrors.purchaseCommission = e;
        else delete nextErrors.purchaseCommission;
        break;
      }
    }
    setErrors(nextErrors);
  };

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    const plate = licensePlate.trim().toUpperCase();
    if (!plate) nextErrors.licensePlate = t("validation.required");
    else if (!PLATE_REGEX.test(plate)) nextErrors.licensePlate = t("validation.invalidPlate");
    if (!formValues.year) nextErrors.year = t("validation.required");
    if (!formValues.color) nextErrors.color = t("validation.required");
    if (!formValues.model) nextErrors.model = t("validation.required");
    if (!formValues.brand) nextErrors.brand = t("validation.required");
    const purchasePriceError = getMoneyError(formValues.purchasePrice, true);
    if (purchasePriceError) nextErrors.purchasePrice = purchasePriceError;
    const freightCostError = getMoneyError(formValues.freightCost);
    if (freightCostError) nextErrors.freightCost = freightCostError;
    const commissionError = getMoneyError(formValues.purchaseCommission);
    if (commissionError) nextErrors.purchaseCommission = commissionError;
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!validate()) return;
    try {
      const normalizedColor = formValues.color.trim().toUpperCase();
      const normalizedBrand = formValues.brand.trim().toUpperCase();
      const normalizedModel = formValues.model.trim().toUpperCase();
      setFormValues((prev) => ({
        ...prev,
        color: normalizedColor,
        brand: normalizedBrand,
        model: normalizedModel,
      }));
      setIsSubmitting(true);
      const response = await api.post<ApiResponse<{ vehicleId: string }>>("/vehicles", {
        licensePlate: licensePlate.trim().toUpperCase(),
        renavam: renavam || null,
        vin: vin || null,
        year: Number(formValues.year),
        color: normalizedColor,
        model: normalizedModel,
        brand: normalizedBrand,
        supplierSource: formValues.supplierSource,
        purchasePrice: parseMoney(formValues.purchasePrice),
        freightCost: parseMoney(formValues.freightCost || "0"),
        purchaseCommission: parseMoney(formValues.purchaseCommission),
        valorFipe: formValues.valorFipe ? parseMoney(formValues.valorFipe) : null,
      });
      showToast(t("vehicles.created"), "success");
      navigate(`/vehicles/${response.data.data.vehicleId}`);
    } catch (error) {
      if ((error as any)?.response?.data?.errors) {
        setErrors(extractFieldErrors((error as any).response.data.errors));
      }
      showToast(extractErrorMessage(error), "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold">{t("newVehicle.title")}</h2>
        <p className="text-sm text-slate-500">{t("newVehicle.subtitle")}</p>
      </div>
      <form
        className="space-y-4 rounded-lg border border-slate-200 bg-white p-6 shadow-sm"
        onSubmit={handleSubmit}
      >
        <div className="grid gap-4 md:grid-cols-2">
          <TextInput
            label={t("newVehicle.licensePlate")}
            value={licensePlate}
            required
            onChange={(e) => setLicensePlate(e.target.value)}
            onBlur={() => {
              const normalized = licensePlate.trim().toUpperCase();
              setLicensePlate(normalized);
              const nextErrors = { ...errors };
              if (!normalized) nextErrors.licensePlate = t("validation.required");
              else if (!PLATE_REGEX.test(normalized)) nextErrors.licensePlate = t("validation.invalidPlate");
              else delete nextErrors.licensePlate;
              setErrors(nextErrors);
            }}
            error={errors.licensePlate}
          />
          <TextInput
            label={t("newVehicle.renavam")}
            value={renavam}
            onChange={(e) => setRenavam(e.target.value)}
            error={errors.renavam}
          />
          <TextInput
            label={t("newVehicle.vin")}
            value={vin}
            onChange={(e) => setVin(e.target.value)}
            error={errors.vin}
          />

          <VehicleFormFields
            values={formValues}
            errors={errors}
            brandOptions={brandOptions}
            colorOptions={colorOptions}
            onChange={handleFormChange}
            onBlur={handleFormBlur}
          />
        </div>

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate("/vehicles")}
            disabled={isSubmitting}
            className="rounded-md border border-slate-200 px-4 py-2 text-sm"
          >
            {t("actions.cancel")}
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {isSubmitting ? t("actions.saving") : t("newVehicle.save")}
          </button>
        </div>
      </form>
    </div>
  );
}
