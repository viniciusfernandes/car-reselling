import { useEffect, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../service/api";
import {
  ApiResponse,
  MarcaItem,
  CorItem,
  DocumentoItem,
  ListaDocumentosResponse,
  ParceiroItem,
  ListaParceirosResponse,
  ServicoItem,
  ListaServicosResponse,
  DetalheVeiculo,
  ImpostosVeiculo,
} from "../../service/types";
import { useToast } from "../../component/notification/ToastProvider";
import { fetchMarcas, fetchCores } from "../../service/marcaModelos";
import DetalheVeiculoHeader from "./component/DetalheVeiculoHeader";
import VeiculoDeleteModal from "./component/VeiculoDeleteModal";
import VeiculoOverviewTab from "./component/VeiculoOverviewTab";
import VeiculoServicosTab from "./component/VeiculoServicosTab";
import VeiculoDocumentosTab from "./component/VeiculoDocumentosTab";
import ImpostosVeiculoTab from "./component/ImpostosVeiculoTab";

type TabKey = "overview" | "servicos" | "documentos" | "impostos";

export default function DetalheVeiculoPage() {
  const { t } = useTranslation();
  const { veiculoId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { showToast } = useToast();

  const initialTab = (location.state as { tab?: TabKey } | null)?.tab ?? "overview";
  const [activeTab, setActiveTab] = useState<TabKey>(initialTab);
  const [veiculo, setVeiculo] = useState<DetalheVeiculo | null>(null);
  const [servicos, setServicos] = useState<ServicoItem[]>([]);
  const [totalServicos, setTotalServicos] = useState(0);
  const [documentos, setDocumentos] = useState<DocumentoItem[]>([]);
  const [parceiros, setParceiros] = useState<ParceiroItem[]>([]);
  const [impostos, setImpostos] = useState<ImpostosVeiculo | null>(null);
  const [corOptions, setCorOptions] = useState<CorItem[]>([]);
  const [marcaOptions, setMarcaOptions] = useState<MarcaItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const tabLabels: Record<TabKey, string> = {
    overview: t("tabs.overview"),
    servicos: t("tabs.services"),
    documentos: t("tabs.documents"),
    impostos: t("tabs.taxes"),
  };

  const fetchAll = async () => {
    if (!veiculoId) return;
    setLoading(true);
    const [veiculoResponse, servicoResponse, documentoResponse, parceiroResponse, impostosResponse] =
      await Promise.allSettled([
        api.get<ApiResponse<DetalheVeiculo>>(`/veiculos/${veiculoId}`),
        api.get<ApiResponse<ListaServicosResponse>>(`/veiculos/${veiculoId}/servicos`),
        api.get<ApiResponse<ListaDocumentosResponse>>(`/veiculos/${veiculoId}/documentos`),
        api.get<ApiResponse<ListaParceirosResponse>>(`/parceiros`),
        api.get<ApiResponse<ImpostosVeiculo>>(`/veiculos/${veiculoId}/impostos`),
      ]);

    if (veiculoResponse.status === "fulfilled") {
      setVeiculo(veiculoResponse.value.data.data);
    } else {
      setVeiculo(null);
      showToast(extractErrorMessage(veiculoResponse.reason), "error");
    }

    if (servicoResponse.status === "fulfilled") {
      setServicos(servicoResponse.value.data.data.servicos);
      setTotalServicos(servicoResponse.value.data.data.total);
    } else {
      showToast(extractErrorMessage(servicoResponse.reason), "error");
    }

    if (documentoResponse.status === "fulfilled") {
      setDocumentos(documentoResponse.value.data.data.documentos);
    } else {
      showToast(extractErrorMessage(documentoResponse.reason), "error");
    }

    if (parceiroResponse.status === "fulfilled") {
      setParceiros(parceiroResponse.value.data.data.parceiros);
    } else {
      showToast(extractErrorMessage(parceiroResponse.reason), "error");
    }

    if (impostosResponse.status === "fulfilled") {
      setImpostos(impostosResponse.value.data.data);
    } else {
      setImpostos(null);
    }

    setLoading(false);
  };

  useEffect(() => {
    fetchAll();
  }, [veiculoId]);

  useEffect(() => {
    const loadLookupData = async () => {
      try {
        const [marcas, cores] = await Promise.all([fetchMarcas(), fetchCores()]);
        setMarcaOptions(marcas);
        setCorOptions(cores);
      } catch (error) {
        showToast(extractErrorMessage(error), "error");
      }
    };
    loadLookupData();
  }, [showToast]);

  const handleDeleteConfirm = async () => {
    if (!veiculoId) return;
    try {
      setDeleting(true);
      await api.delete(`/veiculos/${veiculoId}`);
      showToast(t("vehicles.delete.success"), "success");
      navigate("/veiculos");
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

  if (!veiculo) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-6 text-sm text-slate-500 shadow-sm">
        {t("vehicleDetail.unavailable")}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <DetalheVeiculoHeader
        veiculo={veiculo}
        totalServicos={totalServicos}
        onDeleteClick={() => setShowDeleteModal(true)}
      />

      {showDeleteModal ? (
        <VeiculoDeleteModal
          veiculo={veiculo}
          deleting={deleting}
          onClose={() => setShowDeleteModal(false)}
          onConfirm={handleDeleteConfirm}
        />
      ) : null}

      <div className="flex gap-2 border-b border-slate-200">
        {(["overview", "servicos", "documentos", "impostos"] as TabKey[]).map((tab) => (
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
        <VeiculoOverviewTab
          veiculoId={veiculoId!}
          veiculo={veiculo}
          parceiros={parceiros}
          marcaOptions={marcaOptions}
          corOptions={corOptions}
          documentos={documentos}
          onRefresh={fetchAll}
        />
      ) : null}

      {activeTab === "servicos" ? (
        <VeiculoServicosTab
          veiculoId={veiculoId!}
          servicos={servicos}
          onRefresh={fetchAll}
        />
      ) : null}

      {activeTab === "documentos" ? (
        <VeiculoDocumentosTab
          veiculoId={veiculoId!}
          documentos={documentos}
          onRefresh={fetchAll}
        />
      ) : null}

      {activeTab === "impostos" ? (
        <ImpostosVeiculoTab veiculo={veiculo} impostos={impostos} />
      ) : null}
    </div>
  );
}
