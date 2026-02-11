import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  api,
  extractErrorMessage,
  extractFieldErrors,
} from "../../service/api";
import {
  ApiResponse,
  BrandItem,
  DocumentItem,
  DocumentListResponse,
  DocumentType,
  ModelItem,
  PartnerItem,
  PartnerListResponse,
  ServiceItem,
  ServiceListResponse,
  ServiceType,
  VehicleDetail,
  VehicleTaxes,
  VehicleStatus,
} from "../../service/types";
import TextInput from "../../component/input/TextInput";
import NumberInput from "../../component/input/NumberInput";
import SelectInput from "../../component/input/SelectInput";
import MoneyInput from "../../component/input/MoneyInput";
import ComboboxInput from "../../component/input/ComboboxInput";
import DateInput from "../../component/input/DateInput";
import { useToast } from "../../component/notification/ToastProvider";
import { fetchVehicleSuggestions } from "../../service/vehicleSuggestions";
import { fetchBrands, fetchModelsByBrand } from "../../service/brandModels";
import {
  formatDate,
  formatMoney,
  formatNumber,
  normalizeMoney,
  parseMoney,
} from "../../service/formatters";

const STATUS_KEYS: Record<VehicleStatus, string> = {
  IN_LOT: "status.IN_LOT",
  IN_SERVICE: "status.IN_SERVICE",
  READY_FOR_DISTRIBUTION: "status.READY_FOR_DISTRIBUTION",
  DISTRIBUTED: "status.DISTRIBUTED",
  SOLD: "status.SOLD",
};

type TabKey = "overview" | "services" | "documents" | "distribution" | "taxes";

const normalizeMoneyInput = (value: string) => {
  const sanitized = normalizeMoney(value);
  const [integerPart, decimalPart = ""] = sanitized.split(".");
  const normalizedDecimal = decimalPart.slice(0, 2);
  return normalizedDecimal.length > 0
    ? `${integerPart}.${normalizedDecimal}`
    : integerPart;
};

const formatMoneyValue = (value: string) => {
  if (!value) {
    return "";
  }
  const numeric = Number(normalizeMoney(value));
  if (Number.isNaN(numeric)) {
    return value;
  }
  return formatNumber(numeric);
};

export default function VehicleDetailPage() {
  const { t, i18n } = useTranslation();
  const { vehicleId } = useParams();
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState<TabKey>("overview");
  const [vehicle, setVehicle] = useState<VehicleDetail | null>(null);
  const [services, setServices] = useState<ServiceItem[]>([]);
  const [servicesTotal, setServicesTotal] = useState(0);
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [taxes, setTaxes] = useState<VehicleTaxes | null>(null);
  const [colorSuggestions, setColorSuggestions] = useState<string[]>([]);
  const [brandOptions, setBrandOptions] = useState<BrandItem[]>([]);
  const [modelOptions, setModelOptions] = useState<ModelItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [isUpdatingVehicle, setIsUpdatingVehicle] = useState(false);
  const [isAddingService, setIsAddingService] = useState(false);
  const [isUpdatingService, setIsUpdatingService] = useState(false);
  const [isUploadingDocument, setIsUploadingDocument] = useState(false);
  const [isAssigningPartner, setIsAssigningPartner] = useState(false);
  const [serviceForm, setServiceForm] = useState({
    serviceType: "MECHANICAL" as ServiceType,
    serviceValue: "",
    description: "",
    performedAt: "",
  });
  const [serviceErrors, setServiceErrors] = useState<Record<string, string>>({});
  const [editServiceId, setEditServiceId] = useState<string | null>(null);
  const [editServiceForm, setEditServiceForm] = useState({
    serviceType: "MECHANICAL" as ServiceType,
    serviceValue: "",
    description: "",
    performedAt: "",
  });
  const [editServiceErrors, setEditServiceErrors] = useState<Record<string, string>>(
    {}
  );
  const [documentType, setDocumentType] = useState<DocumentType>("INVOICE");
  const [documentFile, setDocumentFile] = useState<File | null>(null);
  const [updateErrors, setUpdateErrors] = useState<Record<string, string>>({});
  const [updateForm, setUpdateForm] = useState({
    year: "",
    color: "",
    model: "",
    brand: "",
    supplierSource: "INTERNET",
    purchasePrice: "",
    freightCost: "",
    purchaseCommission: "",
    purchaseInvoiceDocumentId: "",
    purchasePaymentReceiptDocumentId: "",
  });
  const [partnerId, setPartnerId] = useState("");
  const [statusTarget, setStatusTarget] = useState<VehicleStatus>("IN_LOT");
  const [sellingPrice, setSellingPrice] = useState("");

  const isDistributed = vehicle?.status === "DISTRIBUTED";
  const tabLabels: Record<TabKey, string> = {
    overview: t("tabs.overview"),
    services: t("tabs.services"),
    documents: t("tabs.documents"),
    distribution: t("tabs.distribution"),
    taxes: t("tabs.taxes"),
  };
  const statusOptions: Array<{ value: VehicleStatus; label: string }> = [
    { value: "IN_LOT", label: t("status.IN_LOT") },
    { value: "IN_SERVICE", label: t("status.IN_SERVICE") },
    { value: "READY_FOR_DISTRIBUTION", label: t("status.READY_FOR_DISTRIBUTION") },
    { value: "DISTRIBUTED", label: t("status.DISTRIBUTED") },
    { value: "SOLD", label: t("status.SOLD") },
  ];
  const serviceOptions: Array<{ value: ServiceType; label: string }> = [
    { value: "MECHANICAL", label: t("serviceTypes.MECHANICAL") },
    { value: "PAINT", label: t("serviceTypes.PAINT") },
    { value: "BODYWORK", label: t("serviceTypes.BODYWORK") },
    { value: "ELECTRICAL", label: t("serviceTypes.ELECTRICAL") },
    { value: "UPHOLSTERY", label: t("serviceTypes.UPHOLSTERY") },
    { value: "WINDOWS", label: t("serviceTypes.WINDOWS") },
  ];
  const documentOptionsLabels: Array<{ value: DocumentType; label: string }> = [
    { value: "INVOICE", label: t("documentTypes.INVOICE") },
    { value: "RECEIPT", label: t("documentTypes.RECEIPT") },
    { value: "SERVICE_ORDER", label: t("documentTypes.SERVICE_ORDER") },
    { value: "OTHER", label: t("documentTypes.OTHER") },
  ];

  const fetchAll = async () => {
    if (!vehicleId) {
      return;
    }
      setLoading(true);
    const [
      vehicleResponse,
      serviceResponse,
      documentResponse,
      partnerResponse,
      taxesResponse,
    ] = await Promise.allSettled([
          api.get<ApiResponse<VehicleDetail>>(`/vehicles/${vehicleId}`),
      api.get<ApiResponse<ServiceListResponse>>(`/vehicles/${vehicleId}/services`),
      api.get<ApiResponse<DocumentListResponse>>(`/vehicles/${vehicleId}/documents`),
          api.get<ApiResponse<PartnerListResponse>>(`/partners`),
          api.get<ApiResponse<VehicleTaxes>>(`/vehicles/${vehicleId}/taxes`),
        ]);

    if (vehicleResponse.status === "fulfilled") {
      setVehicle(vehicleResponse.value.data.data);
    } else {
      setVehicle(null);
      showToast(extractErrorMessage(vehicleResponse.reason));
    }

    if (serviceResponse.status === "fulfilled") {
      setServices(serviceResponse.value.data.data.services);
      setServicesTotal(serviceResponse.value.data.data.total);
    } else {
      showToast(extractErrorMessage(serviceResponse.reason));
    }

    if (documentResponse.status === "fulfilled") {
      setDocuments(documentResponse.value.data.data.documents);
    } else {
      showToast(extractErrorMessage(documentResponse.reason));
    }

    if (partnerResponse.status === "fulfilled") {
      setPartners(partnerResponse.value.data.data.partners);
    } else {
      showToast(extractErrorMessage(partnerResponse.reason));
    }

    if (taxesResponse.status === "fulfilled") {
      setTaxes(taxesResponse.value.data.data);
    } else {
      setTaxes(null);
    }

      setLoading(false);
  };

  useEffect(() => {
    fetchAll();
  }, [vehicleId]);

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
    const selectedBrand = brandOptions.find((brand) => brand.name === updateForm.brand);
    if (!selectedBrand) {
      setModelOptions([]);
      return;
    }
    const loadModels = async () => {
      try {
        const models = await fetchModelsByBrand(selectedBrand.id);
        setModelOptions(models);
        if (
          updateForm.model &&
          !models.some((model) => model.name === updateForm.model)
        ) {
          setUpdateForm((prev) => ({ ...prev, model: "" }));
        }
      } catch (error) {
        showToast(extractErrorMessage(error));
      }
    };
    loadModels();
  }, [brandOptions, updateForm.brand, updateForm.model, showToast]);

  useEffect(() => {
    if (!vehicle) {
      return;
    }
    setUpdateForm({
      year: vehicle.year.toString(),
      color: vehicle.color,
      model: vehicle.model,
      brand: vehicle.brand,
      supplierSource: vehicle.supplierSource,
      purchasePrice: formatNumber(vehicle.purchasePrice),
      freightCost: formatNumber(vehicle.freightCost),
      purchaseCommission: formatNumber(vehicle.purchaseCommission ?? 0),
      purchaseInvoiceDocumentId: vehicle.purchaseInvoiceDocumentId ?? "",
      purchasePaymentReceiptDocumentId:
        vehicle.purchasePaymentReceiptDocumentId ?? "",
    });
    setPartnerId(vehicle.assignedPartnerId ?? "");
    setStatusTarget(vehicle.status);
    setSellingPrice(
      vehicle.sellingPrice !== null && vehicle.sellingPrice !== undefined
        ? formatNumber(vehicle.sellingPrice)
        : ""
    );
  }, [vehicle]);

  const documentTypeLabels = useMemo(
    () =>
      documentOptionsLabels.reduce<Record<string, string>>((acc, option) => {
        acc[option.value] = option.label;
        return acc;
      }, {}),
    [documentOptionsLabels]
  );

  const documentOptions = useMemo(
    () =>
      documents.map((doc) => ({
        value: doc.id,
        label: `${documentTypeLabels[doc.documentType] ?? doc.documentType} - ${
          doc.originalFileName
        }`,
      })),
    [documents, documentTypeLabels]
  );

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

  const validateUpdateForm = () => {
    const nextErrors: Record<string, string> = {};
    if (!updateForm.year) {
      nextErrors.year = t("validation.required");
    }
    if (!updateForm.color) {
      nextErrors.color = t("validation.required");
    }
    if (!updateForm.model) {
      nextErrors.model = t("validation.required");
    }
    if (!updateForm.brand) {
      nextErrors.brand = t("validation.required");
    }
    const purchaseError = getMoneyError(updateForm.purchasePrice, true);
    if (purchaseError) {
      nextErrors.purchasePrice = purchaseError;
    }
    const freightError = getMoneyError(updateForm.freightCost, true);
    if (freightError) {
      nextErrors.freightCost = freightError;
    }
    const commissionError = getMoneyError(updateForm.purchaseCommission, true);
    if (commissionError) {
      nextErrors.purchaseCommission = commissionError;
    }
    setUpdateErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const validateUpdateField = (field: keyof typeof updateForm, value?: string) => {
    const nextErrors = { ...updateErrors };
    if (field === "year") {
      if (!(value ?? updateForm.year)) {
        nextErrors.year = t("validation.required");
      } else {
        delete nextErrors.year;
      }
    }
    if (field === "color") {
      if (!(value ?? updateForm.color)) {
        nextErrors.color = t("validation.required");
      } else {
        delete nextErrors.color;
      }
    }
    if (field === "model") {
      if (!(value ?? updateForm.model)) {
        nextErrors.model = t("validation.required");
      } else {
        delete nextErrors.model;
      }
    }
    if (field === "brand") {
      if (!(value ?? updateForm.brand)) {
        nextErrors.brand = t("validation.required");
      } else {
        delete nextErrors.brand;
      }
    }
    if (field === "purchasePrice") {
      const error = getMoneyError(value ?? updateForm.purchasePrice, true);
      if (error) {
        nextErrors.purchasePrice = error;
      } else {
        delete nextErrors.purchasePrice;
      }
    }
    if (field === "freightCost") {
      const error = getMoneyError(value ?? updateForm.freightCost, true);
      if (error) {
        nextErrors.freightCost = error;
      } else {
        delete nextErrors.freightCost;
      }
    }
    if (field === "purchaseCommission") {
      const error = getMoneyError(value ?? updateForm.purchaseCommission, true);
      if (error) {
        nextErrors.purchaseCommission = error;
      } else {
        delete nextErrors.purchaseCommission;
      }
    }
    setUpdateErrors(nextErrors);
  };

  const validateServiceValue = (value: string) => {
    const error = getMoneyError(value, true);
    return error ? { serviceValue: error } : {};
  };

  const handleAddService = async () => {
    if (!vehicleId) {
      return;
    }
    const nextErrors = validateServiceValue(serviceForm.serviceValue);
    if (Object.keys(nextErrors).length > 0) {
      setServiceErrors(nextErrors);
      return;
    }
    try {
      setIsAddingService(true);
      await api.post(`/vehicles/${vehicleId}/services`, {
        serviceType: serviceForm.serviceType,
        serviceValue: parseMoney(serviceForm.serviceValue),
        description: serviceForm.description || null,
        performedAt: serviceForm.performedAt || null,
      });
      showToast(t("vehicleDetail.services.added"));
      setServiceForm({
        serviceType: "MECHANICAL",
        serviceValue: "",
        description: "",
        performedAt: "",
      });
      setServiceErrors({});
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsAddingService(false);
    }
  };

  const startEditService = (service: ServiceItem) => {
    setEditServiceId(service.id);
    setEditServiceErrors({});
    setEditServiceForm({
      serviceType: service.serviceType,
      serviceValue: formatNumber(service.serviceValue),
      description: service.description ?? "",
      performedAt: service.performedAt ?? "",
    });
  };

  const handleUpdateService = async () => {
    if (!vehicleId || !editServiceId) {
      return;
    }
    const nextErrors = validateServiceValue(editServiceForm.serviceValue);
    if (Object.keys(nextErrors).length > 0) {
      setEditServiceErrors(nextErrors);
      return;
    }
    try {
      setIsUpdatingService(true);
      await api.put(`/vehicles/${vehicleId}/services/${editServiceId}`, {
        serviceType: editServiceForm.serviceType,
        serviceValue: parseMoney(editServiceForm.serviceValue),
        description: editServiceForm.description || null,
        performedAt: editServiceForm.performedAt || null,
      });
      showToast(t("vehicleDetail.services.updated"));
      setEditServiceId(null);
      setEditServiceErrors({});
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsUpdatingService(false);
    }
  };

  const handleDeleteService = async (serviceId: string) => {
    if (!vehicleId) {
      return;
    }
    if (!window.confirm(t("confirm.deleteService"))) {
      return;
    }
    try {
      await api.delete(`/vehicles/${vehicleId}/services/${serviceId}`);
      showToast(t("vehicleDetail.services.deleted"));
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    }
  };

  const handleUploadDocument = async () => {
    if (!vehicleId || !documentFile) {
      return;
    }
    try {
      setIsUploadingDocument(true);
      const formData = new FormData();
      formData.append("documentType", documentType);
      formData.append("file", documentFile);
      await api.post(`/vehicles/${vehicleId}/documents`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      showToast(t("vehicleDetail.documents.uploaded"));
      setDocumentFile(null);
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsUploadingDocument(false);
    }
  };

  const handleDeleteDocument = async (documentId: string) => {
    if (!vehicleId) {
      return;
    }
    if (!window.confirm(t("confirm.deleteDocument"))) {
      return;
    }
    try {
      await api.delete(`/vehicles/${vehicleId}/documents/${documentId}`);
      showToast(t("vehicleDetail.documents.deleted"));
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    }
  };

  const handleUpdateVehicle = async () => {
    if (!vehicleId) {
      return;
    }
    if (statusTarget === "DISTRIBUTED" && !partnerId) {
      showToast(t("vehicleDetail.selectPartnerFirst"));
      return;
    }
    if (statusTarget === "SOLD") {
      const error = getMoneyError(sellingPrice, true);
      if (error) {
        showToast(t("vehicleDetail.setSellingPriceFirst"));
        return;
      }
    }
    if (!validateUpdateForm()) {
      return;
    }
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
        purchaseCommission: parseMoney(updateForm.purchaseCommission),
        purchaseInvoiceDocumentId:
          updateForm.purchaseInvoiceDocumentId || null,
        purchasePaymentReceiptDocumentId:
          updateForm.purchasePaymentReceiptDocumentId || null,
      });
      if (
        (statusTarget === "DISTRIBUTED" || statusTarget === "SOLD") &&
        sellingPrice
      ) {
        await api.put(`/vehicles/${vehicleId}/selling-price`, {
          sellingPrice: parseMoney(sellingPrice),
        });
      }
      if (statusTarget !== vehicle.status) {
        await api.post(`/vehicles/${vehicleId}/status`, {
          status: statusTarget,
          assignedPartnerId: statusTarget === "DISTRIBUTED" ? partnerId : null,
        });
      }
      showToast(t("vehicleDetail.updated"));
      setUpdateErrors({});
      await fetchAll();
    } catch (error: any) {
      if (error?.response?.data?.errors) {
        setUpdateErrors(extractFieldErrors(error.response.data.errors));
      }
      showToast(extractErrorMessage(error));
    } finally {
      setIsUpdatingVehicle(false);
    }
  };

  const handleAssignPartner = async () => {
    if (!vehicleId) {
      return;
    }
    try {
      setIsAssigningPartner(true);
      await api.post(`/vehicles/${vehicleId}/distribution`, {
        partnerId,
      });
      showToast(t("vehicleDetail.distributed"));
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsAssigningPartner(false);
    }
  };

  if (loading) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-6 text-sm text-slate-500 shadow-sm">
        {t("vehicleDetail.loading")}
      </div>
    );
  }

  if (!vehicle) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-6 text-sm text-slate-500 shadow-sm">
        {t("vehicleDetail.unavailable")}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold">
            {vehicle.licensePlate} · {vehicle.brand} {vehicle.model}
          </h2>
          <p className="text-sm text-slate-500">
            {t("vehicleDetail.status", {
              status: t(STATUS_KEYS[vehicle.status]),
            })}
          </p>
        </div>
        <div className="text-right text-sm text-slate-500">
          <div>
            {t("vehicleDetail.servicesTotal", {
              value: formatMoney(servicesTotal),
            })}
          </div>
          <div>
            {t("vehicleDetail.totalCost", { value: formatMoney(vehicle.totalCost) })}
          </div>
        </div>
      </div>

      <div className="flex gap-2 border-b border-slate-200">
        {(["overview", "services", "documents", "distribution", "taxes"] as TabKey[]).map(
          (tab) => (
            <button
              key={tab}
              type="button"
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 text-sm font-medium ${
                activeTab === tab
                  ? "border-b-2 border-slate-900 text-slate-900"
                  : "text-slate-500"
              }`}
            >
              {tabLabels[tab]}
            </button>
          )
        )}
      </div>

      {activeTab === "overview" ? (
        <div className="space-y-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="grid gap-4 md:grid-cols-2">
            <TextInput
              label={t("vehicleDetail.licensePlate")}
              value={vehicle.licensePlate}
              disabled
            />
            <SelectInput
              label={t("vehicleDetail.statusLabel")}
              value={statusTarget}
              options={statusOptions}
              disabled={vehicle.status === "SOLD"}
              onChange={(event) =>
                setStatusTarget(event.target.value as VehicleStatus)
              }
            />
            <SelectInput
              label={t("vehicleDetail.supplierSource")}
              value={updateForm.supplierSource}
              options={[
                { value: "INTERNET", label: t("supplier.internet") },
                { value: "PERSONAL_CONTACT", label: t("supplier.personalContact") },
              ]}
              required
              onChange={(event) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  supplierSource: event.target.value,
                }))
              }
            />
            <NumberInput
              label={t("vehicleDetail.year")}
              value={updateForm.year}
              required
              min={1900}
              max={new Date().getFullYear() + 1}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, year: event.target.value }))
              }
              onBlur={() => validateUpdateField("year")}
              error={updateErrors.year}
            />
            <ComboboxInput
              label={t("vehicleDetail.brand")}
              value={updateForm.brand}
              required
              suggestions={brandOptions.map((brand) => brand.name)}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, brand: event.target.value }))
              }
              onBlur={() => validateUpdateField("brand")}
              error={updateErrors.brand}
            />
            <ComboboxInput
              label={t("vehicleDetail.color")}
              value={updateForm.color}
              required
              suggestions={colorSuggestions}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, color: event.target.value }))
              }
              onBlur={() => validateUpdateField("color")}
              error={updateErrors.color}
            />
            <ComboboxInput
              label={t("vehicleDetail.model")}
              value={updateForm.model}
              required
              suggestions={modelOptions.map((model) => model.name)}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, model: event.target.value }))
              }
              onBlur={() => validateUpdateField("model")}
              error={updateErrors.model}
            />
            <MoneyInput
              label={t("vehicleDetail.purchasePrice")}
              value={updateForm.purchasePrice}
              required
              onValueChange={(value) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  purchasePrice: value,
                }))
              }
              onBlur={() => validateUpdateField("purchasePrice")}
              error={updateErrors.purchasePrice}
            />
            <MoneyInput
              label={t("vehicleDetail.purchaseCommission")}
              value={updateForm.purchaseCommission}
              required
              onValueChange={(value) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  purchaseCommission: value,
                }))
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
              required
              onValueChange={(value) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  freightCost: value,
                }))
              }
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
                    ...partners.map((partner) => ({
                      value: partner.id,
                      label: partner.name,
                    })),
                  ]}
                  onChange={(event) => setPartnerId(event.target.value)}
                />
              ) : null}
            </div>
            <SelectInput
              label={t("vehicleDetail.invoiceDocument")}
              value={updateForm.purchaseInvoiceDocumentId || ""}
              options={[
                { value: "", label: t("vehicleDetail.notLinked") },
                ...documentOptions,
              ]}
              onChange={(event) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  purchaseInvoiceDocumentId: event.target.value,
                }))
              }
            />
            <SelectInput
              label={t("vehicleDetail.paymentReceiptDocument")}
              value={updateForm.purchasePaymentReceiptDocumentId || ""}
              options={[
                { value: "", label: t("vehicleDetail.notLinked") },
                ...documentOptions,
              ]}
              onChange={(event) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  purchasePaymentReceiptDocumentId: event.target.value,
                }))
              }
            />
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
      ) : null}

      {activeTab === "services" ? (
        <div className="space-y-6">
          <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="text-sm font-semibold text-slate-700">
              {t("vehicleDetail.services.addTitle")}
            </h3>
            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <SelectInput
                label={t("vehicleDetail.services.type")}
                value={serviceForm.serviceType}
                options={serviceOptions}
                required
                onChange={(event) =>
                  setServiceForm((prev) => ({
                    ...prev,
                    serviceType: event.target.value as ServiceType,
                  }))
                }
              />
              <MoneyInput
                label={t("vehicleDetail.services.value")}
                value={serviceForm.serviceValue}
                required
                onValueChange={(value) =>
                  setServiceForm((prev) => ({
                    ...prev,
                    serviceValue: value,
                  }))
                }
                onBlur={() =>
                  setServiceErrors(validateServiceValue(serviceForm.serviceValue))
                }
                error={serviceErrors.serviceValue}
              />
              <TextInput
                label={t("vehicleDetail.services.description")}
                value={serviceForm.description}
                onChange={(event) =>
                  setServiceForm((prev) => ({
                    ...prev,
                    description: event.target.value,
                  }))
                }
              />
              <DateInput
                label={t("vehicleDetail.services.performedAt")}
                type="date"
                value={serviceForm.performedAt}
                onChange={(event) =>
                  setServiceForm((prev) => ({
                    ...prev,
                    performedAt: event.target.value,
                  }))
                }
              />
            </div>
            <div className="mt-4 flex justify-end">
              <button
                type="button"
                disabled={isDistributed || isAddingService}
                onClick={handleAddService}
                className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
              >
                {isAddingService
                  ? t("actions.adding")
                  : t("vehicleDetail.services.addAction")}
              </button>
            </div>
            {isDistributed ? (
              <p className="mt-2 text-xs text-slate-500">
                {t("vehicleDetail.services.readOnly")}
              </p>
            ) : null}
          </div>

          <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">{t("vehicleDetail.services.table.type")}</th>
                  <th className="px-4 py-3">{t("vehicleDetail.services.table.date")}</th>
                  <th className="px-4 py-3">{t("vehicleDetail.services.table.value")}</th>
                  <th className="px-4 py-3">
                    {t("vehicleDetail.services.table.description")}
                  </th>
                  <th className="px-4 py-3">{t("vehicleDetail.services.table.actions")}</th>
                </tr>
              </thead>
              <tbody>
                {services.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-6 text-center">
                      {t("vehicleDetail.services.empty")}
                    </td>
                  </tr>
                ) : (
                  services.map((service) => (
                    <tr key={service.id} className="border-t">
                      {editServiceId === service.id ? (
                        <>
                          <td className="px-4 py-3">
                            <select
                              value={editServiceForm.serviceType}
                              onChange={(event) =>
                                setEditServiceForm((prev) => ({
                                  ...prev,
                                  serviceType: event.target.value as ServiceType,
                                }))
                              }
                              className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                            >
                              {serviceOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                  {option.label}
                                </option>
                              ))}
                            </select>
                          </td>
                          <td className="px-4 py-3">
                            <input
                              type="date"
                              lang={i18n.language}
                              value={editServiceForm.performedAt}
                              onChange={(event) =>
                                setEditServiceForm((prev) => ({
                                  ...prev,
                                  performedAt: event.target.value,
                                }))
                              }
                              className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                            />
                          </td>
                          <td className="px-4 py-3">
                            <input
                              type="text"
                              inputMode="decimal"
                              value={editServiceForm.serviceValue}
                              onChange={(event) =>
                                setEditServiceForm((prev) => ({
                                  ...prev,
                                  serviceValue: normalizeMoneyInput(
                                    event.target.value
                                  ),
                                }))
                              }
                              onBlur={() =>
                                setEditServiceForm((prev) => ({
                                  ...prev,
                                  serviceValue: formatMoneyValue(prev.serviceValue),
                                }))
                              }
                              className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                            />
                            {editServiceErrors.serviceValue ? (
                              <span className="text-xs text-red-600">
                                {editServiceErrors.serviceValue}
                              </span>
                            ) : null}
                          </td>
                          <td className="px-4 py-3">
                            <input
                              value={editServiceForm.description}
                              onChange={(event) =>
                                setEditServiceForm((prev) => ({
                                  ...prev,
                                  description: event.target.value,
                                }))
                              }
                              className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                            />
                          </td>
                          <td className="px-4 py-3">
                            <div className="flex gap-2">
                              <button
                                type="button"
                                onClick={handleUpdateService}
                                disabled={isUpdatingService}
                                className="text-xs font-medium text-slate-900 disabled:text-slate-400"
                              >
                                {isUpdatingService
                                  ? t("actions.saving")
                                  : t("actions.save")}
                              </button>
                              <button
                                type="button"
                                onClick={() => {
                                  setEditServiceId(null);
                                  setEditServiceErrors({});
                                }}
                                className="text-xs text-slate-500"
                              >
                                {t("actions.cancel")}
                              </button>
                            </div>
                          </td>
                        </>
                      ) : (
                        <>
                          <td className="px-4 py-3">
                            {t(`serviceTypes.${service.serviceType}`)}
                          </td>
                          <td className="px-4 py-3">
                            {formatDate(service.performedAt)}
                          </td>
                          <td className="px-4 py-3 text-right">
                            {formatMoney(service.serviceValue)}
                          </td>
                          <td className="px-4 py-3">
                            {service.description ?? "-"}
                          </td>
                          <td className="px-4 py-3">
                            <div className="flex gap-2 text-xs">
                              <button
                                type="button"
                                disabled={isDistributed}
                                onClick={() => startEditService(service)}
                                className="text-slate-900 disabled:text-slate-400"
                              >
                                {t("actions.edit")}
                              </button>
                              <button
                                type="button"
                                disabled={isDistributed}
                                onClick={() => handleDeleteService(service.id)}
                                className="text-red-600 disabled:text-slate-400"
                              >
                                {t("actions.delete")}
                              </button>
                            </div>
                          </td>
                        </>
                      )}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}

      {activeTab === "documents" ? (
        <div className="space-y-6">
          <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="text-sm font-semibold text-slate-700">
              {t("vehicleDetail.documents.uploadTitle")}
            </h3>
            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <SelectInput
                label={t("vehicleDetail.documents.type")}
                value={documentType}
                options={documentOptionsLabels}
                onChange={(event) =>
                  setDocumentType(event.target.value as DocumentType)
                }
              />
              <label className="block text-sm">
                <span className="font-medium text-slate-700">
                  {t("vehicleDetail.documents.file")}
                </span>
                <input
                  type="file"
                  onChange={(event) =>
                    setDocumentFile(event.target.files?.[0] ?? null)
                  }
                  className="mt-1 w-full rounded-md border border-slate-200 px-3 py-2 text-sm"
                />
              </label>
            </div>
            <div className="mt-4 flex justify-end">
              <button
                type="button"
                onClick={handleUploadDocument}
                disabled={!documentFile || isUploadingDocument}
                className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
              >
                {isUploadingDocument
                  ? t("actions.uploading")
                  : t("actions.upload")}
              </button>
            </div>
          </div>

          <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">{t("vehicleDetail.documents.table.type")}</th>
                  <th className="px-4 py-3">{t("vehicleDetail.documents.table.file")}</th>
                  <th className="px-4 py-3">{t("vehicleDetail.documents.table.size")}</th>
                  <th className="px-4 py-3">
                    {t("vehicleDetail.documents.table.uploaded")}
                  </th>
                  <th className="px-4 py-3">
                    {t("vehicleDetail.documents.table.actions")}
                  </th>
                </tr>
              </thead>
              <tbody>
                {documents.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-6 text-center">
                      {t("vehicleDetail.documents.empty")}
                    </td>
                  </tr>
                ) : (
                  documents.map((doc) => (
                    <tr key={doc.id} className="border-t">
                      <td className="px-4 py-3">
                        {t(`documentTypes.${doc.documentType}`)}
                      </td>
                      <td className="px-4 py-3">{doc.originalFileName}</td>
                      <td className="px-4 py-3">
                        {t("units.kb", {
                          value: (doc.sizeBytes / 1024).toFixed(1),
                        })}
                      </td>
                      <td className="px-4 py-3">
                        {formatDate(doc.uploadedAt)}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex gap-2 text-xs">
                          <a
                            href={`/api/v1/vehicles/${vehicleId}/documents/${doc.id}/download`}
                            className="text-slate-900"
                          >
                            {t("actions.download")}
                          </a>
                          <button
                            type="button"
                            onClick={() => handleDeleteDocument(doc.id)}
                            className="text-red-600"
                          >
                            {t("actions.delete")}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}

      {activeTab === "distribution" ? (
        <div className="space-y-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div>
            <h3 className="text-sm font-semibold text-slate-700">
              {t("vehicleDetail.distribution.title")}
            </h3>
            <p className="text-sm text-slate-500">
              {t("vehicleDetail.distribution.currentStatus", {
                status: t(STATUS_KEYS[vehicle.status]),
              })}
            </p>
            <p className="text-sm text-slate-500">
              {t("vehicleDetail.distribution.assignedPartner", {
                partner: vehicle.assignedPartnerName ?? "-",
              })}
            </p>
          </div>
          <SelectInput
            label={t("vehicleDetail.distribution.partner")}
            value={partnerId}
            options={[
              { value: "", label: t("vehicleDetail.selectPartner") },
              ...partners.map((partner) => ({
                value: partner.id,
                label: partner.name,
              })),
            ]}
            onChange={(event) => setPartnerId(event.target.value)}
          />
          <div className="flex justify-end">
            <button
              type="button"
              onClick={handleAssignPartner}
              disabled={
                vehicle.status !== "READY_FOR_DISTRIBUTION" ||
                !partnerId ||
                isAssigningPartner
              }
              className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
            >
              {isAssigningPartner
                ? t("actions.assigning")
                : t("vehicleDetail.distribution.assign")}
            </button>
          </div>
          {vehicle.status !== "READY_FOR_DISTRIBUTION" ? (
            <p className="text-xs text-slate-500">
              {t("vehicleDetail.distribution.mustBeReady")}
            </p>
          ) : null}
        </div>
      ) : null}

      {activeTab === "taxes" ? (
        <div className="space-y-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div>
            <h3 className="text-sm font-semibold text-slate-700">
              {t("vehicleDetail.taxes.title")}
            </h3>
            <p className="text-sm text-slate-500">
              {t("vehicleDetail.taxes.subtitle")}
            </p>
          </div>
          {vehicle.sellingPrice !== null && vehicle.sellingPrice !== undefined ? (
            <div className="grid gap-4 md:grid-cols-3">
              <div className="rounded-md border border-slate-100 p-4">
                <div className="text-xs text-slate-500">{t("taxes.icms")}</div>
                <div className="text-base font-semibold">
                  {formatMoney(taxes?.icms ?? 0)}
                </div>
              </div>
              <div className="rounded-md border border-slate-100 p-4">
                <div className="text-xs text-slate-500">{t("taxes.pis")}</div>
                <div className="text-base font-semibold">
                  {formatMoney(taxes?.pis ?? 0)}
                </div>
              </div>
              <div className="rounded-md border border-slate-100 p-4">
                <div className="text-xs text-slate-500">{t("taxes.cofins")}</div>
                <div className="text-base font-semibold">
                  {formatMoney(taxes?.cofins ?? 0)}
                </div>
              </div>
              <div className="rounded-md border border-slate-100 p-4">
                <div className="text-xs text-slate-500">{t("taxes.csll")}</div>
                <div className="text-base font-semibold">
                  {formatMoney(taxes?.csll ?? 0)}
                </div>
              </div>
              <div className="rounded-md border border-slate-100 p-4">
                <div className="text-xs text-slate-500">{t("taxes.irpj")}</div>
                <div className="text-base font-semibold">
                  {formatMoney(taxes?.irpj ?? 0)}
                </div>
              </div>
              <div className="rounded-md border border-slate-100 p-4">
                <div className="text-xs text-slate-500">{t("taxes.total")}</div>
                <div className="text-base font-semibold">
                  {formatMoney(taxes?.totalTaxes ?? 0)}
                </div>
              </div>
            </div>
          ) : (
            <div className="rounded-md border border-dashed border-slate-200 p-4 text-sm text-slate-500">
              {t("vehicleDetail.taxes.missingSellingPrice")}
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
}
