import { useEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage, extractFieldErrors } from "../../../service/api";
import {
  BrandItem,
  ColorItem,
  DocumentItem,
  ModelItem,
  PartnerItem,
  VehicleDetail,
  VehicleStatus,
} from "../../../service/types";
import TextInput from "../../../component/input/TextInput";
import NumberInput from "../../../component/input/NumberInput";
import SelectInput from "../../../component/input/SelectInput";
import MoneyInput from "../../../component/input/MoneyInput";
import ComboboxInput from "../../../component/input/ComboboxInput";
import { useToast } from "../../../component/notification/ToastProvider";
import { fetchModelsByBrand } from "../../../service/brandModels";
import { formatNumber, parseMoney } from "../../../service/formatters";

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
  documents,
  onRefresh,
}: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [modelOptions, setModelOptions] = useState<ModelItem[]>([]);
  const [isUpdatingVehicle, setIsUpdatingVehicle] = useState(false);
  const [statusTarget, setStatusTarget] = useState<VehicleStatus>(vehicle.status);
  const [partnerId, setPartnerId] = useState(vehicle.assignedPartnerId ?? "");
  const [sellingPrice, setSellingPrice] = useState(
    vehicle.sellingPrice != null ? formatNumber(vehicle.sellingPrice) : ""
  );
  const [updateForm, setUpdateForm] = useState({
    year: vehicle.year.toString(),
    color: vehicle.color,
    model: vehicle.model,
    brand: vehicle.brand,
    supplierSource: vehicle.supplierSource,
    purchasePrice: formatNumber(vehicle.purchasePrice),
    freightCost: formatNumber(vehicle.freightCost),
    purchaseCommission: formatNumber(vehicle.purchaseCommission ?? 0),

    purchasePaymentReceiptDocumentId: vehicle.purchasePaymentReceiptDocumentId ?? "",
  });
  const [updateErrors, setUpdateErrors] = useState<Record<string, string>>({});

  const documentTypeLabels: Record<string, string> = {
    INVOICE: t("documentTypes.INVOICE"),
    RECEIPT: t("documentTypes.RECEIPT"),
    SERVICE_ORDER: t("documentTypes.SERVICE_ORDER"),
    OTHER: t("documentTypes.OTHER"),
  };

  const documentOptions = useMemo(
    () =>
      documents.map((doc) => ({
        value: doc.id,
        label: `${documentTypeLabels[doc.documentType] ?? doc.documentType} - ${doc.originalFileName}`,
      })),
    [documents, t]
  );

  const statusOptions: Array<{ value: VehicleStatus; label: string }> = [
    { value: "IN_LOT", label: t("status.IN_LOT") },
    { value: "READY_FOR_DISTRIBUTION", label: t("status.READY_FOR_DISTRIBUTION") },
    { value: "DISTRIBUTED", label: t("status.DISTRIBUTED") },
    { value: "SOLD", label: t("status.SOLD") },
  ];

  useEffect(() => {
    setUpdateForm({
      year: vehicle.year.toString(),
      color: vehicle.color,
      model: vehicle.model,
      brand: vehicle.brand,
      supplierSource: vehicle.supplierSource,
      purchasePrice: formatNumber(vehicle.purchasePrice),
      freightCost: formatNumber(vehicle.freightCost),
      purchaseCommission: formatNumber(vehicle.purchaseCommission ?? 0),
      purchasePaymentReceiptDocumentId: vehicle.purchasePaymentReceiptDocumentId ?? "",
    });
    setPartnerId(vehicle.assignedPartnerId ?? "");
    setStatusTarget(vehicle.status);
    setSellingPrice(vehicle.sellingPrice != null ? formatNumber(vehicle.sellingPrice) : "");
  }, [vehicle]);

  const updateFormModelRef = useRef(updateForm.model);
  updateFormModelRef.current = updateForm.model;

  useEffect(() => {
    const selectedBrand = brandOptions.find((b) => b.name === updateForm.brand);
    if (!selectedBrand) {
      setModelOptions([]);
      return;
    }
    const loadModels = async () => {
      try {
        const models = await fetchModelsByBrand(selectedBrand.id);
        setModelOptions(models);
        if (
          updateFormModelRef.current &&
          !models.some((m) => m.name === updateFormModelRef.current)
        ) {
          setUpdateForm((prev) => ({ ...prev, model: "" }));
        }
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    loadModels();
  }, [brandOptions, updateForm.brand, showToast]);

  const getMoneyError = (value: string, required = false) => {
    if (!value && required) return t("validation.required");
    if (!value) return "";
    const numeric = parseMoney(value);
    if (Number.isNaN(numeric) || numeric < 0) return t("validation.invalidValue");
    return "";
  };

  const validateUpdateForm = () => {
    const nextErrors: Record<string, string> = {};
    if (!updateForm.year) nextErrors.year = t("validation.required");
    if (!updateForm.color) nextErrors.color = t("validation.required");
    if (!updateForm.model) nextErrors.model = t("validation.required");
    if (!updateForm.brand) nextErrors.brand = t("validation.required");
    const purchaseError = getMoneyError(updateForm.purchasePrice, true);
    if (purchaseError) nextErrors.purchasePrice = purchaseError;
    const freightError = getMoneyError(updateForm.freightCost);
    if (freightError) nextErrors.freightCost = freightError;
    const commissionError = getMoneyError(updateForm.purchaseCommission, true);
    if (commissionError) nextErrors.purchaseCommission = commissionError;
    setUpdateErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const validateUpdateField = (field: keyof typeof updateForm, value?: string) => {
    const nextErrors = { ...updateErrors };
    if (field === "year") {
      if (!(value ?? updateForm.year)) nextErrors.year = t("validation.required");
      else delete nextErrors.year;
    }
    if (field === "color") {
      if (!(value ?? updateForm.color)) nextErrors.color = t("validation.required");
      else delete nextErrors.color;
    }
    if (field === "model") {
      if (!(value ?? updateForm.model)) nextErrors.model = t("validation.required");
      else delete nextErrors.model;
    }
    if (field === "brand") {
      if (!(value ?? updateForm.brand)) nextErrors.brand = t("validation.required");
      else delete nextErrors.brand;
    }
    if (field === "purchasePrice") {
      const error = getMoneyError(value ?? updateForm.purchasePrice, true);
      if (error) nextErrors.purchasePrice = error;
      else delete nextErrors.purchasePrice;
    }
    if (field === "freightCost") {
      const error = getMoneyError(value ?? updateForm.freightCost);
      if (error) nextErrors.freightCost = error;
      else delete nextErrors.freightCost;
    }
    if (field === "purchaseCommission") {
      const error = getMoneyError(value ?? updateForm.purchaseCommission, true);
      if (error) nextErrors.purchaseCommission = error;
      else delete nextErrors.purchaseCommission;
    }
    setUpdateErrors(nextErrors);
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
    if (!validateUpdateForm()) return;
    try {
      setIsUpdatingVehicle(true);
      await api.put(`/vehicles/${vehicleId}`, {
        year: Number(updateForm.year),
        color: updateForm.color,
        model: updateForm.model,
        brand: updateForm.brand,
        supplierSource: updateForm.supplierSource,
        purchasePrice: parseMoney(updateForm.purchasePrice),
        freightCost: parseMoney(updateForm.freightCost),
        purchaseCommission: parseMoney(updateForm.purchaseCommission)
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
      setUpdateErrors({});
      await onRefresh();
    } catch (error: any) {
      if (error?.response?.data?.errors) {
        setUpdateErrors(extractFieldErrors(error.response.data.errors));
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
        <SelectInput
          label={t("vehicleDetail.supplierSource")}
          value={updateForm.supplierSource}
          options={[
            { value: "INTERNET", label: t("supplier.internet") },
            { value: "PERSONAL_CONTACT", label: t("supplier.personalContact") },
          ]}
          required
          onChange={(e) => setUpdateForm((prev) => ({ ...prev, supplierSource: e.target.value }))}
        />
        <NumberInput
          label={t("vehicleDetail.year")}
          value={updateForm.year}
          required
          min={1900}
          max={new Date().getFullYear() + 1}
          onChange={(e) => setUpdateForm((prev) => ({ ...prev, year: e.target.value }))}
          onBlur={() => validateUpdateField("year")}
          error={updateErrors.year}
        />
        <ComboboxInput
          label={t("vehicleDetail.brand")}
          value={updateForm.brand}
          required
          suggestions={brandOptions.map((b) => b.name)}
          onChange={(e) => setUpdateForm((prev) => ({ ...prev, brand: e.target.value }))}
          onBlur={() => validateUpdateField("brand")}
          error={updateErrors.brand}
        />
        <ComboboxInput
          label={t("vehicleDetail.model")}
          value={updateForm.model}
          required
          suggestions={modelOptions.map((m) => m.name)}
          onChange={(e) => setUpdateForm((prev) => ({ ...prev, model: e.target.value }))}
          onBlur={() => validateUpdateField("model")}
          error={updateErrors.model}
        />
        <ComboboxInput
          label={t("vehicleDetail.color")}
          value={updateForm.color}
          required
          suggestions={colorOptions.map((c) => c.name)}
          onChange={(e) => setUpdateForm((prev) => ({ ...prev, color: e.target.value }))}
          onBlur={() => {
            const normalized = updateForm.color.trim().toUpperCase();
            setUpdateForm((prev) => ({ ...prev, color: normalized }));
            validateUpdateField("color", normalized);
          }}
          error={updateErrors.color}
        />
        <MoneyInput
          label={t("vehicleDetail.purchasePrice")}
          value={updateForm.purchasePrice}
          required
          onValueChange={(value) => setUpdateForm((prev) => ({ ...prev, purchasePrice: value }))}
          onBlur={() => validateUpdateField("purchasePrice")}
          error={updateErrors.purchasePrice}
        />
        <MoneyInput
          label={t("vehicleDetail.purchaseCommission")}
          value={updateForm.purchaseCommission}
          required
          onValueChange={(value) =>
            setUpdateForm((prev) => ({ ...prev, purchaseCommission: value }))
          }
          onBlur={() => validateUpdateField("purchaseCommission")}
          error={updateErrors.purchaseCommission}
        />
        {vehicle.status === "DISTRIBUTED" ||
        vehicle.status === "SOLD" ||
        statusTarget === "SOLD" ? (
          <div className="space-y-2">
            <MoneyInput
              label={t("vehicleDetail.sellingPrice")}
              value={sellingPrice}
              onValueChange={setSellingPrice}
              required={vehicle.status === "DISTRIBUTED" || statusTarget === "SOLD"}
              disabled={vehicle.status === "SOLD"}
            />
          </div>
        ) : null}
        <MoneyInput
          label={t("vehicleDetail.freightCost")}
          value={updateForm.freightCost}
          onValueChange={(value) => setUpdateForm((prev) => ({ ...prev, freightCost: value }))}
          onBlur={() => validateUpdateField("freightCost")}
          error={updateErrors.freightCost}
        />
        <div className="space-y-3">
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
