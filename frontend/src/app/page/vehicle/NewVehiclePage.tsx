import { useState } from "react";
import { useNavigate } from "react-router-dom";
import TextInput from "../../component/input/TextInput";
import NumberInput from "../../component/input/NumberInput";
import SelectInput from "../../component/input/SelectInput";
import { api, extractErrorMessage, extractFieldErrors } from "../../service/api";
import { ApiResponse, SupplierSource } from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";

const SUPPLIER_OPTIONS: { value: SupplierSource; label: string }[] = [
  { value: "INTERNET", label: "Internet" },
  { value: "PERSONAL_CONTACT", label: "Personal contact" },
];

const PLATE_REGEX =
  /^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$/;

export default function NewVehiclePage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [form, setForm] = useState({
    licensePlate: "",
    renavam: "",
    vin: "",
    year: "",
    color: "",
    model: "",
    brand: "",
    supplierSource: "INTERNET" as SupplierSource,
    purchasePrice: "",
    freightCost: "0",
  });

  const handleChange = (field: keyof typeof form, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
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
    if (!form.purchasePrice || Number(form.purchasePrice) < 0) {
      nextErrors.purchasePrice = "Invalid value";
    }
    if (Number(form.freightCost) < 0) {
      nextErrors.freightCost = "Invalid value";
    }
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!validate()) {
      return;
    }
    try {
      const response = await api.post<ApiResponse<{ vehicleId: string }>>(
        "/vehicles",
        {
          licensePlate: form.licensePlate.trim().toUpperCase(),
          renavam: form.renavam || null,
          vin: form.vin || null,
          year: Number(form.year),
          color: form.color,
          model: form.model,
          brand: form.brand,
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
            onChange={(event) => handleChange("licensePlate", event.target.value)}
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
            onChange={(event) => handleChange("year", event.target.value)}
            error={errors.year}
          />
          <TextInput
            label="Color"
            value={form.color}
            onChange={(event) => handleChange("color", event.target.value)}
            error={errors.color}
          />
          <TextInput
            label="Model"
            value={form.model}
            onChange={(event) => handleChange("model", event.target.value)}
            error={errors.model}
          />
          <TextInput
            label="Brand"
            value={form.brand}
            onChange={(event) => handleChange("brand", event.target.value)}
            error={errors.brand}
          />
          <SelectInput
            label="Supplier Source"
            value={form.supplierSource}
            options={SUPPLIER_OPTIONS}
            onChange={(event) =>
              handleChange("supplierSource", event.target.value)
            }
          />
          <NumberInput
            label="Purchase Price"
            value={form.purchasePrice}
            onChange={(event) =>
              handleChange("purchasePrice", event.target.value)
            }
            error={errors.purchasePrice}
          />
          <NumberInput
            label="Freight Cost"
            value={form.freightCost}
            onChange={(event) =>
              handleChange("freightCost", event.target.value)
            }
            error={errors.freightCost}
          />
        </div>
        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate("/vehicles")}
            className="rounded-md border border-slate-200 px-4 py-2 text-sm"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
          >
            Save Vehicle
          </button>
        </div>
      </form>
    </div>
  );
}
