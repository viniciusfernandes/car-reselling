import { useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../../service/api";
import { DocumentoItem, TipoDocumento } from "../../../service/types";
import SelectInput from "../../../component/input/SelectInput";
import { useToast } from "../../../component/notification/ToastProvider";
import { formatDate } from "../../../service/formatters";

interface Props {
  veiculoId: string;
  documentos: DocumentoItem[];
  onRefresh: () => Promise<void>;
}

export default function VeiculoDocumentosTab({ veiculoId, documentos, onRefresh }: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [tipoDocumento, setTipoDocumento] = useState<TipoDocumento>("NOTA_FISCAL");
  const [documentoFile, setDocumentoFile] = useState<File | null>(null);
  const [isUploadingDocumento, setIsUploadingDocumento] = useState(false);
  const [isDownloadingDocumentoId, setIsDownloadingDocumentoId] = useState<string | null>(null);

  const tipoDocumentoOptions: Array<{ value: TipoDocumento; label: string }> = [
    { value: "NOTA_FISCAL", label: t("documentTypes.NOTA_FISCAL") },
    { value: "COMPROVANTE", label: t("documentTypes.COMPROVANTE") },
    { value: "ORDEM_SERVICO", label: t("documentTypes.ORDEM_SERVICO") },
    { value: "OUTRO", label: t("documentTypes.OUTRO") },
  ];

  const handleUploadDocumento = async () => {
    if (!documentoFile) return;
    try {
      setIsUploadingDocumento(true);
      const formData = new FormData();
      formData.append("tipoDocumento", tipoDocumento);
      formData.append("file", documentoFile);
      await api.post(`/veiculos/${veiculoId}/documentos`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      showToast(t("vehicleDetail.documents.uploaded"), "success");
      setDocumentoFile(null);
      await onRefresh();
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setIsUploadingDocumento(false);
    }
  };

  const handleDownloadDocumento = async (doc: DocumentoItem) => {
    try {
      setIsDownloadingDocumentoId(doc.id);
      const response = await api.get(`/veiculos/${veiculoId}/documentos/${doc.id}/download`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(new Blob([response.data], { type: doc.contentType }));
      const link = document.createElement("a");
      link.href = url;
      link.download = doc.nomeOriginalArquivo;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setIsDownloadingDocumentoId(null);
    }
  };

  const handleDeleteDocumento = async (documentoId: string) => {
    if (!window.confirm(t("confirm.deleteDocument"))) return;
    try {
      await api.delete(`/veiculos/${veiculoId}/documentos/${documentoId}`);
      showToast(t("vehicleDetail.documents.deleted"), "success");
      await onRefresh();
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    }
  };

  return (
    <div className="space-y-6">
      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <h3 className="text-sm font-semibold text-slate-700">
          {t("vehicleDetail.documents.uploadTitle")}
        </h3>
        <div className="mt-4 grid gap-4 md:grid-cols-2">
          <SelectInput
            label={t("vehicleDetail.documents.type")}
            value={tipoDocumento}
            options={tipoDocumentoOptions}
            onChange={(e) => setTipoDocumento(e.target.value as TipoDocumento)}
          />
          <label className="block text-sm">
            <span className="font-medium text-slate-700">
              {t("vehicleDetail.documents.file")}
            </span>
            <input
              type="file"
              onChange={(e) => setDocumentoFile(e.target.files?.[0] ?? null)}
              className="mt-1 w-full rounded-md border border-slate-200 px-3 py-2 text-sm"
            />
          </label>
        </div>
        <div className="mt-4 flex justify-end">
          <button
            type="button"
            onClick={handleUploadDocumento}
            disabled={!documentoFile || isUploadingDocumento}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {isUploadingDocumento ? t("actions.uploading") : t("actions.upload")}
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
              <th className="px-4 py-3">{t("vehicleDetail.documents.table.uploaded")}</th>
              <th className="px-4 py-3">{t("vehicleDetail.documents.table.actions")}</th>
            </tr>
          </thead>
          <tbody>
            {documentos.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center">
                  {t("vehicleDetail.documents.empty")}
                </td>
              </tr>
            ) : (
              documentos.map((doc) => (
                <tr key={doc.id} className="border-t">
                  <td className="px-4 py-3">{t(`documentTypes.${doc.tipoDocumento}`)}</td>
                  <td className="px-4 py-3">{doc.nomeOriginalArquivo}</td>
                  <td className="px-4 py-3">
                    {t("units.kb", { value: (doc.tamanhoBytes / 1024).toFixed(1) })}
                  </td>
                  <td className="px-4 py-3">{formatDate(doc.enviadoEm)}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 text-xs">
                      <button
                        type="button"
                        onClick={() => handleDownloadDocumento(doc)}
                        disabled={isDownloadingDocumentoId === doc.id}
                        className="text-slate-900 disabled:opacity-50"
                      >
                        {isDownloadingDocumentoId === doc.id
                          ? t("actions.downloading")
                          : t("actions.download")}
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDeleteDocumento(doc.id)}
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
  );
}
