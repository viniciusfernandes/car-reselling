import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import TextInput from "../../component/input/TextInput";
import NumberInput from "../../component/input/NumberInput";
import SelectInput from "../../component/input/SelectInput";
import MoneyInput from "../../component/input/MoneyInput";
import ComboboxInput from "../../component/input/ComboboxInput";
import { api, extractErrorMessage, extractFieldErrors } from "../../service/api";
import { ApiResponse, SupplierSource } from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { fetchVehicleSuggestions } from "../../service/vehicleSuggestions";

const SUPPLIER_OPTIONS: { value: SupplierSource; label: string }[] = [
  { value: "INTERNET", label: "Internet" },
  { value: "PERSONAL_CONTACT", label: "Personal contact" },
];

const PLATE_REGEX =
  /^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$/;

export default function NewVehiclePage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const currentYear = new Date().getFullYear().toString();
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [suggestions, setSuggestions] = useState({
    colors: [] as string[],
    brands: [] as string[],
    models: [] as string[],
  });
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
    freightCost: "0.00",
  });

  const handleChange = (field: keyof typeof form, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const getMoneyError = (value: string, required = false) => {
    if (!value && required) {
      return "Required";
    }
    if (!value) {
      return "";
    }
    const numeric = Number(value);
    if (Number.isNaN(numeric) || numeric < 0) {
      return "Invalid value";
    }
    return "";
  };

  const validateField = (field: keyof typeof form, value?: string) => {
    const nextErrors = { ...errors };
    if (field === "licensePlate") {
      const plate = (value ?? form.licensePlate).trim().toUpperCase();
      if (!plate) {
        nextErrors.licensePlate = "Required";
      } else if (!PLATE_REGEX.test(plate)) {
        nextErrors.licensePlate = "Invalid plate format";
      } else {
        delete nextErrors.licensePlate;
      }
    }
    if (field === "year") {
      if (!(value ?? form.year)) {
        nextErrors.year = "Required";
      } else {
        delete nextErrors.year;
      }
    }
    if (field === "color") {
      if (!(value ?? form.color)) {
        nextErrors.color = "Required";
      } else {
        delete nextErrors.color;
      }
    }
    if (field === "model") {
      if (!(value ?? form.model)) {
        nextErrors.model = "Required";
      } else {
        delete nextErrors.model;
      }
    }
    if (field === "brand") {
      if (!(value ?? form.brand)) {
        nextErrors.brand = "Required";
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
    setErrors(nextErrors);
  };

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    const plate = form.licensePlate.trim().toUpperCase();
    if (!plate) {
      nextErrors.licensePlate = "Required";
    } else if (!PLATE_REGEX.test(plate)) {
      nextErrors.licensePlate = "Invalid plate format";
    }
    if (!form.year) {
      nextErrors.year = "Required";
    }
    if (!form.color) {
      nextErrors.color = "Required";
    }
    if (!form.model) {
      nextErrors.model = "Required";
    }
    if (!form.brand) {
      nextErrors.brand = "Required";
    }
    const purchasePriceError = getMoneyError(form.purchasePrice, true);
    if (purchasePriceError) {
      nextErrors.purchasePrice = purchasePriceError;
    }
    const freightCostError = getMoneyError(form.freightCost);
    if (freightCostError) {
      nextErrors.freightCost = freightCostError;
    }
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  useEffect(() => {
    const loadSuggestions = async () => {
      try {
        const response = await fetchVehicleSuggestions();
        setSuggestions(response);
      } catch (error) {
        showToast(extractErrorMessage(error));
      }
    };
    loadSuggestions();
  }, [showToast]);

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
          purchasePrice: Number(form.purchasePrice),
          freightCost: Number(form.freightCost || 0),
        }
      );
      showToast("Vehicle created");
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
        <h2 className="text-xl font-semibold">New Vehicle</h2>
        <p className="text-sm text-slate-500">
          Register a newly acquired vehicle.
        </p>
      </div>
      <form
        className="space-y-4 rounded-lg border border-slate-200 bg-white p-6 shadow-sm"
        onSubmit={handleSubmit}
      >
        <div className="grid gap-4 md:grid-cols-2">
          <TextInput
            label="License Plate"
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
            label="Renavam"
            value={form.renavam}
            onChange={(event) => handleChange("renavam", event.target.value)}
            error={errors.renavam}
          />
          <TextInput
            label="VIN"
            value={form.vin}
            onChange={(event) => handleChange("vin", event.target.value)}
            error={errors.vin}
          />
          <NumberInput
            label="Year"
            value={form.year}
            required
            min={1900}
            max={new Date().getFullYear() + 1}
            onChange={(event) => handleChange("year", event.target.value)}
            onBlur={() => validateField("year")}
            error={errors.year}
          />
          <ComboboxInput
            label="Color"
            value={form.color}
            required
            suggestions={suggestions.colors}
            onChange={(event) => handleChange("color", event.target.value)}
            onBlur={() => {
              const normalized = form.color.trim().toUpperCase();
              handleChange("color", normalized);
              validateField("color", normalized);
            }}
            error={errors.color}
          />
          <ComboboxInput
            label="Model"
            value={form.model}
            required
            suggestions={suggestions.models}
            onChange={(event) => handleChange("model", event.target.value)}
            onBlur={() => {
              const normalized = form.model.trim().toUpperCase();
              handleChange("model", normalized);
              validateField("model", normalized);
            }}
            error={errors.model}
          />
          <ComboboxInput
            label="Brand"
            value={form.brand}
            required
            suggestions={suggestions.brands}
            onChange={(event) => handleChange("brand", event.target.value)}
            onBlur={() => {
              const normalized = form.brand.trim().toUpperCase();
              handleChange("brand", normalized);
              validateField("brand", normalized);
            }}
            error={errors.brand}
          />
          <SelectInput
            label="Supplier Source"
            value={form.supplierSource}
            options={SUPPLIER_OPTIONS}
            required
            onChange={(event) =>
              handleChange("supplierSource", event.target.value)
            }
          />
          <MoneyInput
            label="Purchase Price"
            value={form.purchasePrice}
            required
            onValueChange={(value) => handleChange("purchasePrice", value)}
            onBlur={() => validateField("purchasePrice")}
            error={errors.purchasePrice}
          />
          <MoneyInput
            label="Freight Cost"
            value={form.freightCost}
            onValueChange={(value) => handleChange("freightCost", value)}
            onBlur={() => validateField("freightCost")}
            error={errors.freightCost}
          />
        </div>
        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate("/vehicles")}
            disabled={isSubmitting}
            className="rounded-md border border-slate-200 px-4 py-2 text-sm"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {isSubmitting ? "Saving..." : "Save Vehicle"}
          </button>
        </div>
      </form>
    </div>
  );
}
