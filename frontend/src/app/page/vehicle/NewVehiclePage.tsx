import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import TextInput from "../../component/input/TextInput";
import NumberInput from "../../component/input/NumberInput";
import SelectInput from "../../component/input/SelectInput";
import MoneyInput from "../../component/input/MoneyInput";
import ComboboxInput from "../../component/input/ComboboxInput";
import { api, extractErrorMessage, extractFieldErrors } from "../../service/api";
import { ApiResponse, BrandItem, ModelItem, SupplierSource } from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { fetchVehicleSuggestions } from "../../service/vehicleSuggestions";
import { fetchBrands, fetchModelsByBrand } from "../../service/brandModels";
import { formatNumber, parseMoney } from "../../service/formatters";

const SUPPLIER_OPTIONS: { value: SupplierSource; labelKey: string }[] = [
  { value: "INTERNET", labelKey: "supplier.internet" },
  { value: "PERSONAL_CONTACT", labelKey: "supplier.personalContact" },
];

const PLATE_REGEX =
  /^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$/;

export default function NewVehiclePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const currentYear = new Date().getFullYear().toString();
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [colorSuggestions, setColorSuggestions] = useState<string[]>([]);
  const [brandOptions, setBrandOptions] = useState<BrandItem[]>([]);
  const [modelOptions, setModelOptions] = useState<ModelItem[]>([]);
  const [form, setForm] = useState({
    licensePlate: "",
    renavam: "",
    vin: "",
    year: currentYear,
    color: "",
    model: "",
    brand: "",
    supplierSource: "INTERNET" as SupplierSource,
    purchasePrice: "",
    freightCost: formatNumber(0),
    purchaseCommission: formatNumber(0),
  });

  const handleChange = (field: keyof typeof form, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const getMoneyError = (value: string, required = false) => {
    if (!value && required) {
      return t("validation.required");
    }
    if (!value) {
      return "";
    }
    const numeric = parseMoney(value);
    if (Number.isNaN(numeric) || numeric < 0) {
      return t("validation.invalidValue");
    }
    return "";
  };

  const validateField = (field: keyof typeof form, value?: string) => {
    const nextErrors = { ...errors };
    if (field === "licensePlate") {
      const plate = (value ?? form.licensePlate).trim().toUpperCase();
      if (!plate) {
        nextErrors.licensePlate = t("validation.required");
      } else if (!PLATE_REGEX.test(plate)) {
        nextErrors.licensePlate = t("validation.invalidPlate");
      } else {
        delete nextErrors.licensePlate;
      }
    }
    if (field === "year") {
      if (!(value ?? form.year)) {
        nextErrors.year = t("validation.required");
      } else {
        delete nextErrors.year;
      }
    }
    if (field === "color") {
      if (!(value ?? form.color)) {
        nextErrors.color = t("validation.required");
      } else {
        delete nextErrors.color;
      }
    }
    if (field === "model") {
      if (!(value ?? form.model)) {
        nextErrors.model = t("validation.required");
      } else {
        delete nextErrors.model;
      }
    }
    if (field === "brand") {
      if (!(value ?? form.brand)) {
        nextErrors.brand = t("validation.required");
      } else {
        delete nextErrors.brand;
      }
    }
    if (field === "purchasePrice") {
      const error = getMoneyError(value ?? form.purchasePrice, true);
      if (error) {
        nextErrors.purchasePrice = error;
      } else {
        delete nextErrors.purchasePrice;
      }
    }
    if (field === "freightCost") {
      const error = getMoneyError(value ?? form.freightCost);
      if (error) {
        nextErrors.freightCost = error;
      } else {
        delete nextErrors.freightCost;
      }
    }
    if (field === "purchaseCommission") {
      const error = getMoneyError(value ?? form.purchaseCommission, true);
      if (error) {
        nextErrors.purchaseCommission = error;
      } else {
        delete nextErrors.purchaseCommission;
      }
    }
    setErrors(nextErrors);
  };

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    const plate = form.licensePlate.trim().toUpperCase();
    if (!plate) {
      nextErrors.licensePlate = t("validation.required");
    } else if (!PLATE_REGEX.test(plate)) {
      nextErrors.licensePlate = t("validation.invalidPlate");
    }
    if (!form.year) {
      nextErrors.year = t("validation.required");
    }
    if (!form.color) {
      nextErrors.color = t("validation.required");
    }
    if (!form.model) {
      nextErrors.model = t("validation.required");
    }
    if (!form.brand) {
      nextErrors.brand = t("validation.required");
    }
    const purchasePriceError = getMoneyError(form.purchasePrice, true);
    if (purchasePriceError) {
      nextErrors.purchasePrice = purchasePriceError;
    }
    const freightCostError = getMoneyError(form.freightCost);
    if (freightCostError) {
      nextErrors.freightCost = freightCostError;
    }
    const commissionError = getMoneyError(form.purchaseCommission, true);
    if (commissionError) {
      nextErrors.purchaseCommission = commissionError;
    }
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  useEffect(() => {
    const loadSuggestions = async () => {
      try {
        const response = await fetchVehicleSuggestions();
        setColorSuggestions(response.colors);
      } catch (error) {
        showToast(extractErrorMessage(error));
      }
    };
    loadSuggestions();
  }, [showToast]);

  useEffect(() => {
    const loadBrands = async () => {
      try {
        const brands = await fetchBrands();
        setBrandOptions(brands);
      } catch (error) {
        showToast(extractErrorMessage(error));
      }
    };
    loadBrands();
  }, [showToast]);

  useEffect(() => {
    const selectedBrand = brandOptions.find((brand) => brand.name === form.brand);
    if (!selectedBrand) {
      setModelOptions([]);
      return;
    }
    const loadModels = async () => {
      try {
        const models = await fetchModelsByBrand(selectedBrand.id);
        setModelOptions(models);
        if (form.model && !models.some((model) => model.name === form.model)) {
          handleChange("model", "");
        }
      } catch (error) {
        showToast(extractErrorMessage(error));
      }
    };
    loadModels();
  }, [brandOptions, form.brand, form.model, showToast]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!validate()) {
      return;
    }
    try {
      const normalizedColor = form.color.trim().toUpperCase();
      const normalizedBrand = form.brand.trim().toUpperCase();
      const normalizedModel = form.model.trim().toUpperCase();
      setForm((prev) => ({
        ...prev,
        color: normalizedColor,
        brand: normalizedBrand,
        model: normalizedModel,
      }));
      setIsSubmitting(true);
      const response = await api.post<ApiResponse<{ vehicleId: string }>>(
        "/vehicles",
        {
          licensePlate: form.licensePlate.trim().toUpperCase(),
          renavam: form.renavam || null,
          vin: form.vin || null,
          year: Number(form.year),
          color: normalizedColor,
          model: normalizedModel,
          brand: normalizedBrand,
          supplierSource: form.supplierSource,
          purchasePrice: parseMoney(form.purchasePrice),
          freightCost: parseMoney(form.freightCost || "0"),
          purchaseCommission: parseMoney(form.purchaseCommission),
        }
      );
      showToast(t("vehicles.created"));
      navigate(`/vehicles/${response.data.data.vehicleId}`);
    } catch (error) {
      if ((error as any)?.response?.data?.errors) {
        setErrors(extractFieldErrors((error as any).response.data.errors));
      }
      showToast(extractErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold">{t("newVehicle.title")}</h2>
        <p className="text-sm text-slate-500">
          {t("newVehicle.subtitle")}
        </p>
      </div>
      <form
        className="space-y-4 rounded-lg border border-slate-200 bg-white p-6 shadow-sm"
        onSubmit={handleSubmit}
      >
        <div className="grid gap-4 md:grid-cols-2">
          <TextInput
            label={t("newVehicle.licensePlate")}
            value={form.licensePlate}
            required
            onChange={(event) => handleChange("licensePlate", event.target.value)}
            onBlur={() => {
              const normalizedPlate = form.licensePlate.trim().toUpperCase();
              handleChange("licensePlate", normalizedPlate);
              validateField("licensePlate", normalizedPlate);
            }}
            error={errors.licensePlate}
          />
          <TextInput
            label={t("newVehicle.renavam")}
            value={form.renavam}
            onChange={(event) => handleChange("renavam", event.target.value)}
            error={errors.renavam}
          />
          <TextInput
            label={t("newVehicle.vin")}
            value={form.vin}
            onChange={(event) => handleChange("vin", event.target.value)}
            error={errors.vin}
          />
          <NumberInput
            label={t("newVehicle.year")}
            value={form.year}
            required
            min={1900}
            max={new Date().getFullYear() + 1}
            onChange={(event) => handleChange("year", event.target.value)}
            onBlur={() => validateField("year")}
            error={errors.year}
          />
          <ComboboxInput
            label={t("newVehicle.color")}
            value={form.color}
            required
            suggestions={colorSuggestions}
            onChange={(event) => handleChange("color", event.target.value)}
            onBlur={() => {
              const normalized = form.color.trim().toUpperCase();
              handleChange("color", normalized);
              validateField("color", normalized);
            }}
            error={errors.color}
          />
          <ComboboxInput
            label={t("newVehicle.model")}
            value={form.model}
            required
            suggestions={modelOptions.map((model) => model.name)}
            onChange={(event) => handleChange("model", event.target.value)}
            onBlur={() => {
              const normalized = form.model.trim().toUpperCase();
              handleChange("model", normalized);
              validateField("model", normalized);
            }}
            error={errors.model}
          />
          <ComboboxInput
            label={t("newVehicle.brand")}
            value={form.brand}
            required
            suggestions={brandOptions.map((brand) => brand.name)}
            onChange={(event) => handleChange("brand", event.target.value)}
            onBlur={() => {
              const normalized = form.brand.trim().toUpperCase();
              handleChange("brand", normalized);
              validateField("brand", normalized);
            }}
            error={errors.brand}
          />
          <SelectInput
            label={t("newVehicle.supplierSource")}
            value={form.supplierSource}
            options={SUPPLIER_OPTIONS.map((option) => ({
              value: option.value,
              label: t(option.labelKey),
            }))}
            required
            onChange={(event) =>
              handleChange("supplierSource", event.target.value)
            }
          />
          <MoneyInput
            label={t("newVehicle.purchasePrice")}
            value={form.purchasePrice}
            required
            onValueChange={(value) => handleChange("purchasePrice", value)}
            onBlur={() => validateField("purchasePrice")}
            error={errors.purchasePrice}
          />
          <MoneyInput
            label={t("newVehicle.freightCost")}
            value={form.freightCost}
            onValueChange={(value) => handleChange("freightCost", value)}
            onBlur={() => validateField("freightCost")}
            error={errors.freightCost}
          />
          <MoneyInput
            label={t("newVehicle.purchaseCommission")}
            value={form.purchaseCommission}
            required
            onValueChange={(value) => handleChange("purchaseCommission", value)}
            onBlur={() => validateField("purchaseCommission")}
            error={errors.purchaseCommission}
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
