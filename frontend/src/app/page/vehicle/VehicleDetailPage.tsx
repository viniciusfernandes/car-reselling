import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  BrandItem,
  ColorItem,
  DocumentItem,
  DocumentListResponse,
  PartnerItem,
  PartnerListResponse,
  ServiceItem,
  ServiceListResponse,
  VehicleDetail,
  VehicleTaxes,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { fetchBrands, fetchColors } from "../../service/brandModels";
import VehicleDetailHeader from "./component/VehicleDetailHeader";
import VehicleDeleteModal from "./component/VehicleDeleteModal";
import VehicleOverviewTab from "./component/VehicleOverviewTab";
import VehicleServicesTab from "./component/VehicleServicesTab";
import VehicleDocumentsTab from "./component/VehicleDocumentsTab";
import VehicleTaxesTab from "./component/VehicleTaxesTab";

type TabKey = "overview" | "services" | "documents" | "taxes";

export default function VehicleDetailPage() {
  const { t } = useTranslation();
  const { vehicleId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState<TabKey>("overview");
  const [vehicle, setVehicle] = useState<VehicleDetail | null>(null);
  const [services, setServices] = useState<ServiceItem[]>([]);
  const [servicesTotal, setServicesTotal] = useState(0);
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [taxes, setTaxes] = useState<VehicleTaxes | null>(null);
  const [colorOptions, setColorOptions] = useState<ColorItem[]>([]);
  const [brandOptions, setBrandOptions] = useState<BrandItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const tabLabels: Record<TabKey, string> = {
    overview: t("tabs.overview"),
    services: t("tabs.services"),
    documents: t("tabs.documents"),
    taxes: t("tabs.taxes"),
  };

  const fetchAll = async () => {
    if (!vehicleId) return;
    setLoading(true);
    const [vehicleResponse, serviceResponse, documentResponse, partnerResponse, taxesResponse] =
      await Promise.allSettled([
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
      showToast(extractErrorMessage(vehicleResponse.reason), "error");
    }

    if (serviceResponse.status === "fulfilled") {
      setServices(serviceResponse.value.data.data.services);
      setServicesTotal(serviceResponse.value.data.data.total);
    } else {
      showToast(extractErrorMessage(serviceResponse.reason), "error");
    }

    if (documentResponse.status === "fulfilled") {
      setDocuments(documentResponse.value.data.data.documents);
    } else {
      showToast(extractErrorMessage(documentResponse.reason), "error");
    }

    if (partnerResponse.status === "fulfilled") {
      setPartners(partnerResponse.value.data.data.partners);
    } else {
      showToast(extractErrorMessage(partnerResponse.reason), "error");
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

  const handleDeleteConfirm = async () => {
    if (!vehicleId) return;
    try {
      setDeleting(true);
      await api.delete(`/vehicles/${vehicleId}`);
      showToast(t("vehicles.delete.success"), "success");
      navigate("/vehicles");
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
      setDeleting(false);
      setShowDeleteModal(false);
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
      <VehicleDetailHeader
        vehicle={vehicle}
        servicesTotal={servicesTotal}
        onDeleteClick={() => setShowDeleteModal(true)}
      />

      {showDeleteModal ? (
        <VehicleDeleteModal
          vehicle={vehicle}
          deleting={deleting}
          onClose={() => setShowDeleteModal(false)}
          onConfirm={handleDeleteConfirm}
        />
      ) : null}

      <div className="flex gap-2 border-b border-slate-200">
        {(["overview", "services", "documents", "taxes"] as TabKey[]).map((tab) => (
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
        ))}
      </div>

      {activeTab === "overview" ? (
        <VehicleOverviewTab
          vehicleId={vehicleId!}
          vehicle={vehicle}
          partners={partners}
          brandOptions={brandOptions}
          colorOptions={colorOptions}
          documents={documents}
          onRefresh={fetchAll}
        />
      ) : null}

      {activeTab === "services" ? (
        <VehicleServicesTab
          vehicleId={vehicleId!}
          services={services}
          onRefresh={fetchAll}
        />
      ) : null}

      {activeTab === "documents" ? (
        <VehicleDocumentsTab
          vehicleId={vehicleId!}
          documents={documents}
          onRefresh={fetchAll}
        />
      ) : null}

      {activeTab === "taxes" ? (
        <VehicleTaxesTab vehicle={vehicle} taxes={taxes} />
      ) : null}
    </div>
  );
}
