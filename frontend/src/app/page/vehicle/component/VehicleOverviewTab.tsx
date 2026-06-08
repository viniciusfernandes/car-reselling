import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage, extractFieldErrors } from "../../../service/api";
import {
  BrandItem,
  ColorItem,
  DocumentItem,
  PartnerItem,
  VehicleDetail,
  VehicleStatus,
} from "../../../service/types";
import { useToast } from "../../../component/notification/ToastProvider";
import MoneyInput from "../../../component/input/MoneyInput";
import SelectInput from "../../../component/input/SelectInput";
import TextInput from "../../../component/input/TextInput";
import { formatNumber, parseMoney } from "../../../service/formatters";
import VehicleFormFields, { VehicleFormValues } from "./VehicleFormFields";

interface Props {
  vehicleId: string;
  vehicle: VehicleDetail;
  partners: PartnerItem[];
  brandOptions: BrandItem[];
  colorOptions: ColorItem[];
  documents: DocumentItem[];
  onRefresh: () => Promise<void>;
}

export default function VehicleOverviewTab({
  vehicleId,
  vehicle,
  partners,
  brandOptions,
  colorOptions,
  onRefresh,
}: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [isUpdatingVehicle, setIsUpdatingVehicle] = useState(false);
  const [statusTarget, setStatusTarget] = useState<VehicleStatus>(vehicle.status);
  const [partnerId, setPartnerId] = useState(vehicle.assignedPartnerId ?? "");
  const [sellingPrice, setSellingPrice] = useState(
    vehicle.sellingPrice != null ? formatNumber(vehicle.sellingPrice) : ""
  );
  const [formValues, setFormValues] = useState<VehicleFormValues>({
    year: vehicle.year.toString(),
    color: vehicle.color,
    model: vehicle.model,
    brand: vehicle.brand,
    supplierSource: vehicle.supplierSource,
    purchasePrice: formatNumber(vehicle.purchasePrice),
    freightCost: formatNumber(vehicle.freightCost),
    purchaseCommission: formatNumber(vehicle.purchaseCommission ?? 0),
    valorFipe: vehicle.valorFipe != null ? formatNumber(vehicle.valorFipe) : "",
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  const statusOptions: Array<{ value: VehicleStatus; label: string }> = [
    { value: "IN_LOT", label: t("status.IN_LOT") },
    { value: "READY_FOR_DISTRIBUTION", label: t("status.READY_FOR_DISTRIBUTION") },
    { value: "DISTRIBUTED", label: t("status.DISTRIBUTED") },
    { value: "SOLD", label: t("status.SOLD") },
  ];

  useEffect(() => {
    setFormValues({
      year: vehicle.year.toString(),
      color: vehicle.color,
      model: vehicle.model,
      brand: vehicle.brand,
      supplierSource: vehicle.supplierSource,
      purchasePrice: formatNumber(vehicle.purchasePrice),
      freightCost: formatNumber(vehicle.freightCost),
      purchaseCommission: formatNumber(vehicle.purchaseCommission ?? 0),
      valorFipe: vehicle.valorFipe != null ? formatNumber(vehicle.valorFipe) : "",
    });
    setPartnerId(vehicle.assignedPartnerId ?? "");
    setStatusTarget(vehicle.status);
    setSellingPrice(vehicle.sellingPrice != null ? formatNumber(vehicle.sellingPrice) : "");
  }, [vehicle]);

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
    const nextErrors = { ...formErrors };
    let val = currentValue;

    if (field === "color") {
      val = currentValue.trim().toUpperCase();
      setFormValues((prev) => ({ ...prev, color: val }));
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
    if (!formValues.year) nextErrors.year = t("validation.required");
    if (!formValues.color) nextErrors.color = t("validation.required");
    if (!formValues.model) nextErrors.model = t("validation.required");
    if (!formValues.brand) nextErrors.brand = t("validation.required");
    const purchaseError = getMoneyError(formValues.purchasePrice, true);
    if (purchaseError) nextErrors.purchasePrice = purchaseError;
    const freightError = getMoneyError(formValues.freightCost);
    if (freightError) nextErrors.freightCost = freightError;
    const commissionError = getMoneyError(formValues.purchaseCommission, true);
    if (commissionError) nextErrors.purchaseCommission = commissionError;
    setFormErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleUpdateVehicle = async () => {
    if (statusTarget === "DISTRIBUTED" && !partnerId) {
      showToast(t("vehicleDetail.selectPartnerFirst"), "error");
      return;
    }
    if (statusTarget === "SOLD") {
      const error = getMoneyError(sellingPrice, true);
      if (error) {
        showToast(t("vehicleDetail.setSellingPriceFirst"), "error");
        return;
      }
    }
    if (!validateForm()) return;
    try {
      setIsUpdatingVehicle(true);
      await api.put(`/vehicles/${vehicleId}`, {
        year: Number(formValues.year),
        color: formValues.color,
        model: formValues.model,
        brand: formValues.brand,
        supplierSource: formValues.supplierSource,
        purchasePrice: parseMoney(formValues.purchasePrice),
        freightCost: parseMoney(formValues.freightCost),
        purchaseCommission: parseMoney(formValues.purchaseCommission),
        valorFipe: formValues.valorFipe ? parseMoney(formValues.valorFipe) : null,
      });
      if ((statusTarget === "DISTRIBUTED" || statusTarget === "SOLD") && sellingPrice) {
        await api.put(`/vehicles/${vehicleId}/selling-price`, {
          sellingPrice: parseMoney(sellingPrice),
        });
      }
      await api.post(`/vehicles/${vehicleId}/status`, {
        status: statusTarget,
        assignedPartnerId:
          (statusTarget === "DISTRIBUTED" || statusTarget === "SOLD") && partnerId
            ? partnerId
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
      setIsUpdatingVehicle(false);
    }
  };

  return (
    <div className="space-y-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div className="grid gap-4 md:grid-cols-2">
        <TextInput label={t("vehicleDetail.licensePlate")} value={vehicle.licensePlate} disabled />
        <SelectInput
          label={t("vehicleDetail.statusLabel")}
          value={statusTarget}
          options={statusOptions}
          disabled={vehicle.status === "SOLD"}
          onChange={(e) => setStatusTarget(e.target.value as VehicleStatus)}
        />

        <VehicleFormFields
          values={formValues}
          errors={formErrors}
          brandOptions={brandOptions}
          colorOptions={colorOptions}
          onChange={handleFormChange}
          onBlur={handleFormBlur}
        />

        {vehicle.status === "DISTRIBUTED" ||
        vehicle.status === "SOLD" ||
        statusTarget === "SOLD" ? (
          <MoneyInput
            label={t("vehicleDetail.sellingPrice")}
            value={sellingPrice}
            onValueChange={setSellingPrice}
            required={vehicle.status === "DISTRIBUTED" || statusTarget === "SOLD"}
            disabled={vehicle.status === "SOLD"}
          />
        ) : null}

        {statusTarget === "DISTRIBUTED" || statusTarget === "SOLD" ? (
          <SelectInput
            label={t("vehicleDetail.partnerRequired")}
            value={partnerId}
            options={[
              { value: "", label: t("vehicleDetail.selectPartner") },
              ...partners.map((p) => ({ value: p.id, label: p.name })),
            ]}
            onChange={(e) => setPartnerId(e.target.value)}
          />
        ) : null}
      </div>

      <div className="flex justify-end">
        <button
          type="button"
          onClick={handleUpdateVehicle}
          disabled={isUpdatingVehicle}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {isUpdatingVehicle ? t("actions.saving") : t("actions.saveChanges")}
        </button>
      </div>
    </div>
  );
}
