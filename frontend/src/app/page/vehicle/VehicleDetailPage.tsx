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
import { useToast } from "../../component/notification/ToastProvider";

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
};

type TabKey = "overview" | "services" | "documents" | "distribution";

export default function VehicleDetailPage() {
  const { vehicleId } = useParams();
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState<TabKey>("overview");
  const [vehicle, setVehicle] = useState<VehicleDetail | null>(null);
  const [services, setServices] = useState<ServiceItem[]>([]);
  const [servicesTotal, setServicesTotal] = useState(0);
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [serviceForm, setServiceForm] = useState({
    serviceType: "MECHANICAL" as ServiceType,
    serviceValue: "",
    description: "",
    performedAt: "",
  });
  const [editServiceId, setEditServiceId] = useState<string | null>(null);
  const [editServiceForm, setEditServiceForm] = useState({
    serviceType: "MECHANICAL" as ServiceType,
    serviceValue: "",
    description: "",
    performedAt: "",
  });
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

  const isDistributed = vehicle?.status === "DISTRIBUTED";

  const fetchAll = async () => {
    if (!vehicleId) {
      return;
    }
    try {
      setLoading(true);
      const [vehicleResponse, serviceResponse, documentResponse, partnerResponse] =
        await Promise.all([
          api.get<ApiResponse<VehicleDetail>>(`/vehicles/${vehicleId}`),
          api.get<ApiResponse<ServiceListResponse>>(
            `/vehicles/${vehicleId}/services`
          ),
          api.get<ApiResponse<DocumentListResponse>>(
            `/vehicles/${vehicleId}/documents`
          ),
          api.get<ApiResponse<PartnerListResponse>>(`/partners`),
        ]);
      setVehicle(vehicleResponse.data.data);
      setServices(serviceResponse.data.data.services);
      setServicesTotal(serviceResponse.data.data.total);
      setDocuments(documentResponse.data.data.documents);
      setPartners(partnerResponse.data.data.partners);
    } catch (error) {
      showToast(extractErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
  }, [vehicleId]);

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
  }, [vehicle]);

  const documentOptions = useMemo(
    () =>
      documents.map((doc) => ({
        value: doc.id,
        label: `${doc.documentType} - ${doc.originalFileName}`,
      })),
    [documents]
  );

  const handleAddService = async () => {
    if (!vehicleId) {
      return;
    }
    try {
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
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    }
  };

  const startEditService = (service: ServiceItem) => {
    setEditServiceId(service.id);
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
    try {
      await api.put(`/vehicles/${vehicleId}/services/${editServiceId}`, {
        serviceType: editServiceForm.serviceType,
        serviceValue: Number(editServiceForm.serviceValue),
        description: editServiceForm.description || null,
        performedAt: editServiceForm.performedAt || null,
      });
      showToast("Service updated");
      setEditServiceId(null);
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
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
    try {
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
    }
  };

  const handleAssignPartner = async () => {
    if (!vehicleId) {
      return;
    }
    try {
      await api.post(`/vehicles/${vehicleId}/distribution`, {
        partnerId,
      });
      showToast("Vehicle distributed");
      await fetchAll();
    } catch (error) {
      showToast(extractErrorMessage(error));
    }
  };

  if (loading || !vehicle) {
    return <div>Loading vehicle...</div>;
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
          <div>Services total: {servicesTotal.toFixed(2)}</div>
          <div>Total cost: {vehicle.totalCost.toFixed(2)}</div>
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
              label="Supplier Source"
              value={updateForm.supplierSource}
              options={[
                { value: "INTERNET", label: "Internet" },
                { value: "PERSONAL_CONTACT", label: "Personal contact" },
              ]}
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
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, year: event.target.value }))
              }
              error={updateErrors.year}
            />
            <TextInput
              label="Color"
              value={updateForm.color}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, color: event.target.value }))
              }
              error={updateErrors.color}
            />
            <TextInput
              label="Model"
              value={updateForm.model}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, model: event.target.value }))
              }
              error={updateErrors.model}
            />
            <TextInput
              label="Brand"
              value={updateForm.brand}
              onChange={(event) =>
                setUpdateForm((prev) => ({ ...prev, brand: event.target.value }))
              }
              error={updateErrors.brand}
            />
            <NumberInput
              label="Purchase Price"
              value={updateForm.purchasePrice}
              onChange={(event) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  purchasePrice: event.target.value,
                }))
              }
              error={updateErrors.purchasePrice}
            />
            <NumberInput
              label="Freight Cost"
              value={updateForm.freightCost}
              onChange={(event) =>
                setUpdateForm((prev) => ({
                  ...prev,
                  freightCost: event.target.value,
                }))
              }
              error={updateErrors.freightCost}
            />
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
              className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
            >
              Save changes
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
                onChange={(event) =>
                  setServiceForm((prev) => ({
                    ...prev,
                    serviceType: event.target.value as ServiceType,
                  }))
                }
              />
              <NumberInput
                label="Service Value"
                value={serviceForm.serviceValue}
                onChange={(event) =>
                  setServiceForm((prev) => ({
                    ...prev,
                    serviceValue: event.target.value,
                  }))
                }
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
                disabled={isDistributed}
                onClick={handleAddService}
                className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
              >
                Add Service
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
                              type="number"
                              value={editServiceForm.serviceValue}
                              onChange={(event) =>
                                setEditServiceForm((prev) => ({
                                  ...prev,
                                  serviceValue: event.target.value,
                                }))
                              }
                              className="w-full rounded border border-slate-200 px-2 py-1 text-sm"
                            />
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
                                className="text-xs font-medium text-slate-900"
                              >
                                Save
                              </button>
                              <button
                                type="button"
                                onClick={() => setEditServiceId(null)}
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
                          <td className="px-4 py-3">
                            {service.serviceValue.toFixed(2)}
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
                className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
              >
                Upload
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
                vehicle.status !== "READY_FOR_DISTRIBUTION" || !partnerId
              }
              className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
            >
              Assign to Partner
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
