import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import {
  api,
  extractErrorMessage,
  extractFieldErrors,
} from "../../service/api";
import {
  ApiResponse,
  DocumentItem,
  DocumentListResponse,
  DocumentType,
  PartnerItem,
  PartnerListResponse,
  ServiceItem,
  ServiceListResponse,
  ServiceType,
  VehicleDetail,
  VehicleStatus,
} from "../../service/types";
import TextInput from "../../component/input/TextInput";
import NumberInput from "../../component/input/NumberInput";
import SelectInput from "../../component/input/SelectInput";
import MoneyInput from "../../component/input/MoneyInput";
import ComboboxInput from "../../component/input/ComboboxInput";
import { useToast } from "../../component/notification/ToastProvider";
import { fetchVehicleSuggestions } from "../../service/vehicleSuggestions";

const SERVICE_OPTIONS: { value: ServiceType; label: string }[] = [
  { value: "MECHANICAL", label: "Mechanical" },
  { value: "PAINT", label: "Paint" },
  { value: "BODYWORK", label: "Bodywork" },
  { value: "ELECTRICAL", label: "Electrical" },
  { value: "UPHOLSTERY", label: "Upholstery" },
  { value: "WINDOWS", label: "Windows" },
];

const DOCUMENT_OPTIONS: { value: DocumentType; label: string }[] = [
  { value: "INVOICE", label: "Invoice" },
  { value: "RECEIPT", label: "Receipt" },
  { value: "SERVICE_ORDER", label: "Service order" },
  { value: "OTHER", label: "Other" },
];

const STATUS_LABELS: Record<VehicleStatus, string> = {
  IN_LOT: "In lot",
  IN_SERVICE: "In service",
  READY_FOR_DISTRIBUTION: "Ready for distribution",
  DISTRIBUTED: "Distributed",
  SOLD: "Sold",
};

type TabKey = "overview" | "services" | "documents" | "distribution";

const formatMoney = (value: number) =>
  new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);

const normalizeMoneyInput = (value: string) => {
  const sanitized = value.replace(",", ".").replace(/[^0-9.]/g, "");
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
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return value;
  }
  return numeric.toFixed(2);
};

export default function VehicleDetailPage() {
  const { vehicleId } = useParams();
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState<TabKey>("overview");
  const [vehicle, setVehicle] = useState<VehicleDetail | null>(null);
  const [services, setServices] = useState<ServiceItem[]>([]);
  const [servicesTotal, setServicesTotal] = useState(0);
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [suggestions, setSuggestions] = useState({
    colors: [] as string[],
    brands: [] as string[],
    models: [] as string[],
  });
  const [loading, setLoading] = useState(true);
  const [isUpdatingVehicle, setIsUpdatingVehicle] = useState(false);
  const [isAddingService, setIsAddingService] = useState(false);
  const [isUpdatingService, setIsUpdatingService] = useState(false);
  const [isUploadingDocument, setIsUploadingDocument] = useState(false);
  const [isAssigningPartner, setIsAssigningPartner] = useState(false);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);
  const [isUpdatingSellingPrice, setIsUpdatingSellingPrice] = useState(false);
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
    purchaseInvoiceDocumentId: "",
    purchasePaymentReceiptDocumentId: "",
  });
  const [partnerId, setPartnerId] = useState("");
  const [statusTarget, setStatusTarget] = useState<VehicleStatus>("IN_LOT");
  const [sellingPrice, setSellingPrice] = useState("");

  const isDistributed = vehicle?.status === "DISTRIBUTED";

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
    ] = await Promise.allSettled([
          api.get<ApiResponse<VehicleDetail>>(`/vehicles/${vehicleId}`),
      api.get<ApiResponse<ServiceListResponse>>(`/vehicles/${vehicleId}/services`),
      api.get<ApiResponse<DocumentListResponse>>(`/vehicles/${vehicleId}/documents`),
          api.get<ApiResponse<PartnerListResponse>>(`/partners`),
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

      setLoading(false);
  };

  useEffect(() => {
    fetchAll();
  }, [vehicleId]);

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
      purchasePrice: vehicle.purchasePrice.toString(),
      freightCost: vehicle.freightCost.toString(),
      purchaseInvoiceDocumentId: vehicle.purchaseInvoiceDocumentId ?? "",
      purchasePaymentReceiptDocumentId:
        vehicle.purchasePaymentReceiptDocumentId ?? "",
    });
    setPartnerId(vehicle.assignedPartnerId ?? "");
    setStatusTarget(vehicle.status);
    setSellingPrice(
      vehicle.sellingPrice !== null && vehicle.sellingPrice !== undefined
        ? vehicle.sellingPrice.toFixed(2)
        : ""
    );
  }, [vehicle]);

  const documentOptions = useMemo(
    () =>
      documents.map((doc) => ({
        value: doc.id,
        label: `${doc.documentType} - ${doc.originalFileName}`,
      })),
    [documents]
  );

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

  const validateUpdateForm = () => {
    const nextErrors: Record<string, string> = {};
    if (!updateForm.year) {
      nextErrors.year = "Required";
    }
    if (!updateForm.color) {
      nextErrors.color = "Required";
    }
    if (!updateForm.model) {
      nextErrors.model = "Required";
    }
    if (!updateForm.brand) {
      nextErrors.brand = "Required";
    }
    const purchaseError = getMoneyError(updateForm.purchasePrice, true);
    if (purchaseError) {
      nextErrors.purchasePrice = purchaseError;
    }
    const freightError = getMoneyError(updateForm.freightCost, true);
    if (freightError) {
      nextErrors.freightCost = freightError;
    }
    setUpdateErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const validateUpdateField = (field: keyof typeof updateForm, value?: string) => {
    const nextErrors = { ...updateErrors };
    if (field === "year") {
      if (!(value ?? updateForm.year)) {
        nextErrors.year = "Required";
      } else {
        delete nextErrors.year;
      }
    }
    if (field === "color") {
      if (!(value ?? updateForm.color)) {
        nextErrors.color = "Required";
      } else {
        delete nextErrors.color;
      }
    }
    if (field === "model") {
      if (!(value ?? updateForm.model)) {
        nextErrors.model = "Required";
      } else {
        delete nextErrors.model;
      }
    }
    if (field === "brand") {
      if (!(value ?? updateForm.brand)) {
        nextErrors.brand = "Required";
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
        serviceValue: Number(serviceForm.serviceValue),
        description: serviceForm.description || null,
        performedAt: serviceForm.performedAt || null,
      });
      showToast("Service added");
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
      serviceValue: service.serviceValue.toString(),
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
        serviceValue: Number(editServiceForm.serviceValue),
        description: editServiceForm.description || null,
        performedAt: editServiceForm.performedAt || null,
      });
      showToast("Service updated");
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
    if (!window.confirm("Delete this service?")) {
      return;
    }
    try {
      await api.delete(`/vehicles/${vehicleId}/services/${serviceId}`);
      showToast("Service deleted");
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
      showToast("Uploaded successfully");
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
    if (!window.confirm("Delete this document?")) {
      return;
    }
    try {
      await api.delete(`/vehicles/${vehicleId}/documents/${documentId}`);
      showToast("Document deleted");
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    }
  };

  const handleUpdateVehicle = async () => {
    if (!vehicleId) {
      return;
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
        purchasePrice: Number(updateForm.purchasePrice),
        freightCost: Number(updateForm.freightCost),
        purchaseInvoiceDocumentId:
          updateForm.purchaseInvoiceDocumentId || null,
        purchasePaymentReceiptDocumentId:
          updateForm.purchasePaymentReceiptDocumentId || null,
      });
      showToast("Vehicle updated");
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
      showToast("Vehicle distributed");
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsAssigningPartner(false);
    }
  };

  const handleUpdateSellingPrice = async () => {
    if (!vehicleId) {
      return;
    }
    const error = getMoneyError(sellingPrice, true);
    if (error) {
      showToast(error);
      return;
    }
    try {
      setIsUpdatingSellingPrice(true);
      await api.put(`/vehicles/${vehicleId}/selling-price`, {
        sellingPrice: Number(sellingPrice),
      });
      showToast("Selling price updated");
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsUpdatingSellingPrice(false);
    }
  };

  const handleMarkSold = async () => {
    if (!vehicleId) {
      return;
    }
    const error = getMoneyError(sellingPrice, true);
    if (error) {
      showToast("Set a selling price before marking as sold.");
      return;
    }
    try {
      setIsUpdatingStatus(true);
      await api.post(`/vehicles/${vehicleId}/status`, {
        status: "SOLD",
        assignedPartnerId: partnerId || null,
      });
      showToast("Vehicle marked as sold");
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  const handleUpdateStatus = async () => {
    if (!vehicleId) {
      return;
    }
    if (statusTarget === "DISTRIBUTED" && !partnerId) {
      showToast("Select a partner before distributing.");
      return;
    }
    try {
      setIsUpdatingStatus(true);
      await api.post(`/vehicles/${vehicleId}/status`, {
        status: statusTarget,
        assignedPartnerId: statusTarget === "DISTRIBUTED" ? partnerId : null,
      });
      showToast("Status updated");
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  if (loading) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-6 text-sm text-slate-500 shadow-sm">
        Loading vehicle details...
      </div>
    );
  }

  if (!vehicle) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-6 text-sm text-slate-500 shadow-sm">
        Vehicle details unavailable. Please refresh.
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
            Status: {STATUS_LABELS[vehicle.status]}
          </p>
        </div>
        <div className="text-right text-sm text-slate-500">
          <div>Services total: {formatMoney(servicesTotal)}</div>
          <div>Total cost: {formatMoney(vehicle.totalCost)}</div>
        </div>
      </div>

      <div className="flex gap-2 border-b border-slate-200">
        {(["overview", "services", "documents", "distribution"] as TabKey[]).map(
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
              {tab.charAt(0).toUpperCase() + tab.slice(1)}
            </button>
          )
        )}
      </div>

      {activeTab === "overview" ? (
        <div className="space-y-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="grid gap-4 md:grid-cols-2">
            <TextInput
              label="License Plate"
              value={vehicle.licensePlate}
              disabled
            />
            <SelectInput
              label="Status"
              value={statusTarget}
              options={[
                { value: "IN_LOT", label: "In lot" },
                { value: "IN_SERVICE", label: "In service" },
                { value: "READY_FOR_DISTRIBUTION", label: "Ready for distribution" },
                { value: "DISTRIBUTED", label: "Distributed" },
                { value: "SOLD", label: "Sold" },
              ]}
              disabled={vehicle.status === "SOLD"}
              onChange={(event) =>
                setStatusTarget(event.target.value as VehicleStatus)
              }
            />
            <SelectInput
              label="Supplier Source"
              value={updateForm.supplierSource}
              options={[
                { value: "INTERNET", label: "Internet" },
                { value: "PERSONAL_CONTACT", label: "Personal contact" },
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
              label="Year"
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
              label="Color"
              value={updateForm.color}
              required
              suggestions={suggestions.colors}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, color: event.target.value }))
              }
              onBlur={() => validateUpdateField("color")}
              error={updateErrors.color}
            />
            <ComboboxInput
              label="Model"
              value={updateForm.model}
              required
              suggestions={suggestions.models}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, model: event.target.value }))
              }
              onBlur={() => validateUpdateField("model")}
              error={updateErrors.model}
            />
            <ComboboxInput
              label="Brand"
              value={updateForm.brand}
              required
              suggestions={suggestions.brands}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, brand: event.target.value }))
              }
              onBlur={() => validateUpdateField("brand")}
              error={updateErrors.brand}
            />
            <MoneyInput
              label="Purchase Price"
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
            {vehicle.status === "DISTRIBUTED" || vehicle.status === "SOLD" ? (
              <div className="space-y-2">
                <MoneyInput
                  label="Selling Price"
                  value={sellingPrice}
                  onValueChange={setSellingPrice}
                  onBlur={() =>
                    setSellingPrice((value) => (value ? Number(value).toFixed(2) : value))
                  }
                  required={vehicle.status === "DISTRIBUTED"}
                  disabled={vehicle.status === "SOLD"}
                />
                {vehicle.status === "DISTRIBUTED" ? (
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={handleUpdateSellingPrice}
                      disabled={isUpdatingSellingPrice}
                      className="rounded-md border border-slate-200 px-4 py-2 text-sm disabled:cursor-not-allowed disabled:text-slate-300"
                    >
                      {isUpdatingSellingPrice ? "Saving..." : "Save selling price"}
                    </button>
                    <button
                      type="button"
                      onClick={handleMarkSold}
                      disabled={isUpdatingStatus || !sellingPrice}
                      className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
                    >
                      {isUpdatingStatus ? "Updating..." : "Sold"}
                    </button>
                  </div>
                ) : null}
              </div>
            ) : null}
            <MoneyInput
              label="Freight Cost"
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
              {statusTarget === "DISTRIBUTED" ? (
                <SelectInput
                  label="Partner (required for distribution)"
                  value={partnerId}
                  options={[
                    { value: "", label: "Select partner" },
                    ...partners.map((partner) => ({
                      value: partner.id,
                      label: partner.name,
                    })),
                  ]}
                  onChange={(event) => setPartnerId(event.target.value)}
                />
              ) : null}
              <button
                type="button"
                onClick={handleUpdateStatus}
                disabled={isUpdatingStatus || statusTarget === vehicle.status}
                className="rounded-md border border-slate-200 px-4 py-2 text-sm disabled:cursor-not-allowed disabled:text-slate-300"
              >
                {isUpdatingStatus ? "Updating..." : "Update Status"}
              </button>
            </div>
            <SelectInput
              label="Invoice Document"
              value={updateForm.purchaseInvoiceDocumentId || ""}
              options={[{ value: "", label: "Not linked" }, ...documentOptions]}
              onChange={(event) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  purchaseInvoiceDocumentId: event.target.value,
                }))
              }
            />
            <SelectInput
              label="Payment Receipt Document"
              value={updateForm.purchasePaymentReceiptDocumentId || ""}
              options={[{ value: "", label: "Not linked" }, ...documentOptions]}
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
              {isUpdatingVehicle ? "Saving..." : "Save changes"}
            </button>
          </div>
        </div>
      ) : null}

      {activeTab === "services" ? (
        <div className="space-y-6">
          <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="text-sm font-semibold text-slate-700">Add Service</h3>
            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <SelectInput
                label="Service Type"
                value={serviceForm.serviceType}
                options={SERVICE_OPTIONS}
                required
                onChange={(event) =>
                  setServiceForm((prev) => ({
                    ...prev,
                    serviceType: event.target.value as ServiceType,
                  }))
                }
              />
              <MoneyInput
                label="Service Value"
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
                label="Description"
                value={serviceForm.description}
                onChange={(event) =>
                  setServiceForm((prev) => ({
                    ...prev,
                    description: event.target.value,
                  }))
                }
              />
              <TextInput
                label="Performed At"
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
                {isAddingService ? "Adding..." : "Add Service"}
              </button>
            </div>
            {isDistributed ? (
              <p className="mt-2 text-xs text-slate-500">
                Services are read-only after distribution.
              </p>
            ) : null}
          </div>

          <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Type</th>
                  <th className="px-4 py-3">Date</th>
                  <th className="px-4 py-3">Value</th>
                  <th className="px-4 py-3">Description</th>
                  <th className="px-4 py-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                {services.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-6 text-center">
                      No services yet.
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
                              {SERVICE_OPTIONS.map((option) => (
                                <option key={option.value} value={option.value}>
                                  {option.label}
                                </option>
                              ))}
                            </select>
                          </td>
                          <td className="px-4 py-3">
                            <input
                              type="date"
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
                                {isUpdatingService ? "Saving..." : "Save"}
                              </button>
                              <button
                                type="button"
                                onClick={() => {
                                  setEditServiceId(null);
                                  setEditServiceErrors({});
                                }}
                                className="text-xs text-slate-500"
                              >
                                Cancel
                              </button>
                            </div>
                          </td>
                        </>
                      ) : (
                        <>
                          <td className="px-4 py-3">{service.serviceType}</td>
                          <td className="px-4 py-3">
                            {service.performedAt ?? "-"}
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
                                Edit
                              </button>
                              <button
                                type="button"
                                disabled={isDistributed}
                                onClick={() => handleDeleteService(service.id)}
                                className="text-red-600 disabled:text-slate-400"
                              >
                                Delete
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
              Upload Document
            </h3>
            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <SelectInput
                label="Document Type"
                value={documentType}
                options={DOCUMENT_OPTIONS}
                onChange={(event) =>
                  setDocumentType(event.target.value as DocumentType)
                }
              />
              <label className="block text-sm">
                <span className="font-medium text-slate-700">File</span>
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
                {isUploadingDocument ? "Uploading..." : "Upload"}
              </button>
            </div>
          </div>

          <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Type</th>
                  <th className="px-4 py-3">File</th>
                  <th className="px-4 py-3">Size</th>
                  <th className="px-4 py-3">Uploaded</th>
                  <th className="px-4 py-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                {documents.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-6 text-center">
                      No documents uploaded.
                    </td>
                  </tr>
                ) : (
                  documents.map((doc) => (
                    <tr key={doc.id} className="border-t">
                      <td className="px-4 py-3">{doc.documentType}</td>
                      <td className="px-4 py-3">{doc.originalFileName}</td>
                      <td className="px-4 py-3">
                        {(doc.sizeBytes / 1024).toFixed(1)} KB
                      </td>
                      <td className="px-4 py-3">
                        {new Date(doc.uploadedAt).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex gap-2 text-xs">
                          <a
                            href={`/api/v1/vehicles/${vehicleId}/documents/${doc.id}/download`}
                            className="text-slate-900"
                          >
                            Download
                          </a>
                          <button
                            type="button"
                            onClick={() => handleDeleteDocument(doc.id)}
                            className="text-red-600"
                          >
                            Delete
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
            <h3 className="text-sm font-semibold text-slate-700">Distribution</h3>
            <p className="text-sm text-slate-500">
              Current status: {STATUS_LABELS[vehicle.status]}
            </p>
            <p className="text-sm text-slate-500">
              Assigned partner: {vehicle.assignedPartnerName ?? "-"}
            </p>
          </div>
          <SelectInput
            label="Partner"
            value={partnerId}
            options={[
              { value: "", label: "Select partner" },
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
              {isAssigningPartner ? "Assigning..." : "Assign to Partner"}
            </button>
          </div>
          {vehicle.status !== "READY_FOR_DISTRIBUTION" ? (
            <p className="text-xs text-slate-500">
              Vehicle must be ready for distribution.
            </p>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
