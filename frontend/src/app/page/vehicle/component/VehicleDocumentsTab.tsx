import { useState } from "react";
import { useTranslation } from "react-i18next";
import { api, extractErrorMessage } from "../../../service/api";
import { DocumentItem, DocumentType } from "../../../service/types";
import SelectInput from "../../../component/input/SelectInput";
import { useToast } from "../../../component/notification/ToastProvider";
import { formatDate } from "../../../service/formatters";

interface Props {
  vehicleId: string;
  documents: DocumentItem[];
  onRefresh: () => Promise<void>;
}

export default function VehicleDocumentsTab({ vehicleId, documents, onRefresh }: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [documentType, setDocumentType] = useState<DocumentType>("INVOICE");
  const [documentFile, setDocumentFile] = useState<File | null>(null);
  const [isUploadingDocument, setIsUploadingDocument] = useState(false);
  const [isDownloadingDocumentId, setIsDownloadingDocumentId] = useState<string | null>(null);

  const documentTypeOptions: Array<{ value: DocumentType; label: string }> = [
    { value: "INVOICE", label: t("documentTypes.INVOICE") },
    { value: "RECEIPT", label: t("documentTypes.RECEIPT") },
    { value: "SERVICE_ORDER", label: t("documentTypes.SERVICE_ORDER") },
    { value: "OTHER", label: t("documentTypes.OTHER") },
  ];

  const handleUploadDocument = async () => {
    if (!documentFile) return;
    try {
      setIsUploadingDocument(true);
      const formData = new FormData();
      formData.append("documentType", documentType);
      formData.append("file", documentFile);
      await api.post(`/vehicles/${vehicleId}/documents`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      showToast(t("vehicleDetail.documents.uploaded"), "success");
      setDocumentFile(null);
      await onRefresh();
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setIsUploadingDocument(false);
    }
  };

  const handleDownloadDocument = async (doc: DocumentItem) => {
    try {
      setIsDownloadingDocumentId(doc.id);
      const response = await api.get(`/vehicles/${vehicleId}/documents/${doc.id}/download`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(new Blob([response.data], { type: doc.contentType }));
      const link = document.createElement("a");
      link.href = url;
      link.download = doc.originalFileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setIsDownloadingDocumentId(null);
    }
  };

  const handleDeleteDocument = async (documentId: string) => {
    if (!window.confirm(t("confirm.deleteDocument"))) return;
    try {
      await api.delete(`/vehicles/${vehicleId}/documents/${documentId}`);
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
            value={documentType}
            options={documentTypeOptions}
            onChange={(e) => setDocumentType(e.target.value as DocumentType)}
          />
          <label className="block text-sm">
            <span className="font-medium text-slate-700">
              {t("vehicleDetail.documents.file")}
            </span>
            <input
              type="file"
              onChange={(e) => setDocumentFile(e.target.files?.[0] ?? null)}
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
            {isUploadingDocument ? t("actions.uploading") : t("actions.upload")}
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
            {documents.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center">
                  {t("vehicleDetail.documents.empty")}
                </td>
              </tr>
            ) : (
              documents.map((doc) => (
                <tr key={doc.id} className="border-t">
                  <td className="px-4 py-3">{t(`documentTypes.${doc.documentType}`)}</td>
                  <td className="px-4 py-3">{doc.originalFileName}</td>
                  <td className="px-4 py-3">
                    {t("units.kb", { value: (doc.sizeBytes / 1024).toFixed(1) })}
                  </td>
                  <td className="px-4 py-3">{formatDate(doc.uploadedAt)}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 text-xs">
                      <button
                        type="button"
                        onClick={() => handleDownloadDocument(doc)}
                        disabled={isDownloadingDocumentId === doc.id}
                        className="text-slate-900 disabled:opacity-50"
                      >
                        {isDownloadingDocumentId === doc.id
                          ? t("actions.downloading")
                          : t("actions.download")}
                      </button>
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
  );
}
