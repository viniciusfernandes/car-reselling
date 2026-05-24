import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import type { PaymentDocumentItem } from "../../../service/types";
import { paymentsApi, extractErrorMessage } from "../../../service/api";
import { useToast } from "../../../component/notification/ToastProvider";

type Props = {
  paymentId: string;
  paymentLabel: string;
  onClose: () => void;
};

export default function PaymentDocumentsModal({ paymentId, paymentLabel, onClose }: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();
  const dialogRef = useRef<HTMLDialogElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [documents, setDocuments] = useState<PaymentDocumentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  useEffect(() => {
    dialogRef.current?.showModal();
    fetchDocuments();
  }, []);

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      const response = await paymentsApi.listDocuments(paymentId);
      setDocuments(response.data.data.documents);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      setUploading(true);
      await paymentsApi.uploadDocument(paymentId, file);
      showToast(t("payments.documents.uploaded"), "success");
      await fetchDocuments();
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleDownload = async (doc: PaymentDocumentItem) => {
    try {
      setDownloadingId(doc.id);
      const response = await paymentsApi.downloadDocument(paymentId, doc.id);
      const url = URL.createObjectURL(response.data as Blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = doc.originalFileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setDownloadingId(null);
    }
  };

  const handleDelete = async (docId: string) => {
    try {
      setDeletingId(docId);
      await paymentsApi.deleteDocument(paymentId, docId);
      showToast(t("payments.documents.deleted"), "success");
      setDocuments((prev) => prev.filter((d) => d.id !== docId));
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setDeletingId(null);
      setConfirmDeleteId(null);
    }
  };

  const formatSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <dialog
      ref={dialogRef}
      onClose={onClose}
      className="w-full max-w-lg rounded-xl shadow-xl backdrop:bg-black/40 p-0 border-0"
    >
      <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
        <div>
          <h3 className="font-semibold text-slate-800">{t("payments.documents.title")}</h3>
          <p className="text-xs text-slate-500">{paymentLabel}</p>
        </div>
        <button
          type="button"
          onClick={() => dialogRef.current?.close()}
          className="text-slate-400 hover:text-slate-600 text-xl leading-none"
          aria-label={t("actions.cancel")}
        >
          ×
        </button>
      </div>

      <div className="px-6 py-5 space-y-4">
        <div>
          <input
            ref={fileInputRef}
            type="file"
            id="payment-doc-upload"
            className="hidden"
            onChange={handleFileChange}
          />
          <label
            htmlFor="payment-doc-upload"
            className={`inline-flex cursor-pointer items-center gap-2 rounded-md border border-dashed border-slate-300 px-4 py-2 text-sm text-slate-600 hover:border-slate-400 hover:bg-slate-50 transition ${uploading ? "opacity-50 pointer-events-none" : ""}`}
          >
            <span>📎</span>
            {uploading ? t("actions.uploading") : t("payments.documents.upload")}
          </label>
        </div>

        {loading ? (
          <div className="py-4 text-center text-sm text-slate-400">{t("common.loading")}</div>
        ) : documents.length === 0 ? (
          <div className="rounded-md border border-dashed border-slate-200 py-6 text-center text-sm text-slate-400">
            {t("payments.documents.empty")}
          </div>
        ) : (
          <div className="divide-y divide-slate-100 rounded-lg border border-slate-200">
            {documents.map((doc) => (
              <div key={doc.id} className="flex items-center justify-between gap-2 px-4 py-3">
                <div className="min-w-0 flex-1">
                  <div className="truncate text-sm font-medium text-slate-700">
                    {doc.originalFileName}
                  </div>
                  <div className="text-xs text-slate-400">
                    {formatSize(doc.sizeBytes)} · {new Date(doc.uploadedAt).toLocaleDateString()}
                  </div>
                </div>
                <div className="flex shrink-0 gap-2">
                  <button
                    type="button"
                    onClick={() => handleDownload(doc)}
                    disabled={downloadingId === doc.id}
                    className="rounded-md border border-slate-200 px-3 py-1 text-xs hover:bg-slate-50 disabled:opacity-50"
                  >
                    {downloadingId === doc.id ? t("actions.downloading") : t("actions.download")}
                  </button>
                  {confirmDeleteId === doc.id ? (
                    <div className="flex gap-1">
                      <button
                        type="button"
                        onClick={() => handleDelete(doc.id)}
                        disabled={deletingId === doc.id}
                        className="rounded-md bg-red-600 px-3 py-1 text-xs text-white hover:bg-red-700 disabled:opacity-50"
                      >
                        {t("actions.delete")}
                      </button>
                      <button
                        type="button"
                        onClick={() => setConfirmDeleteId(null)}
                        className="rounded-md border border-slate-200 px-3 py-1 text-xs hover:bg-slate-50"
                      >
                        {t("actions.cancel")}
                      </button>
                    </div>
                  ) : (
                    <button
                      type="button"
                      onClick={() => setConfirmDeleteId(doc.id)}
                      className="rounded-md border border-red-200 px-3 py-1 text-xs text-red-600 hover:bg-red-50"
                    >
                      {t("actions.delete")}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="flex justify-end border-t border-slate-200 px-6 py-4">
        <button
          type="button"
          onClick={() => dialogRef.current?.close()}
          className="rounded-md border border-slate-200 px-4 py-2 text-sm hover:bg-slate-50"
        >
          {t("actions.cancel")}
        </button>
      </div>
    </dialog>
  );
}
