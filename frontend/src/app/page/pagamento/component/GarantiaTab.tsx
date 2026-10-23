import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type { ApiResponse, PagamentoItem, ListaVeiculosResponse } from "../../../service/types";
import { pagamentosApi, api, extractErrorMessage } from "../../../service/api";
import { formatMoney, formatDate } from "../../../service/formatters";
import { useToast } from "../../../component/notification/ToastProvider";
import DateInput from "../../../component/input/DateInput";
import PagamentoFormModal from "./PagamentoFormModal";
import DocumentosPagamentoModal from "./DocumentosPagamentoModal";

export default function GarantiaTab() {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [pagamentos, setPagamentos] = useState<PagamentoItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editItem, setEditItem] = useState<PagamentoItem | null>(null);
  const [docsItem, setDocsItem] = useState<PagamentoItem | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);
  const [filterPlate, setFilterPlate] = useState("");
  const [filterMonth, setFilterMonth] = useState("");

  const fetchPagamentos = useCallback(async () => {
    try {
      setLoading(true);
      const res = await pagamentosApi.list({
        tipoPagamento: "GARANTIA",
        placa: filterPlate || undefined,
        mesPagamento: filterMonth || undefined,
      });
      setPagamentos(res.data.data.pagamentos);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setLoading(false);
    }
  }, [filterPlate, filterMonth, showToast]);

  useEffect(() => {
    fetchPagamentos();
  }, [fetchPagamentos]);

  const handleDelete = async (id: string) => {
    try {
      setDeletingId(id);
      await pagamentosApi.delete(id);
      showToast(t("payments.deleted"), "success");
      setPagamentos((prev) => prev.filter((p) => p.id !== id));
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setDeletingId(null);
      setConfirmDeleteId(null);
    }
  };

  const total = pagamentos.reduce((sum, p) => sum + p.valor, 0);

  return (
    <div className="space-y-4">
      {/* Filters + Add button */}
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div className="flex flex-wrap gap-3 items-end">
          <label className="block text-sm">
            <span className="font-medium text-slate-700">{t("payments.warranty.filterPlate")}</span>
            <input
              type="text"
              value={filterPlate}
              onChange={(e) => setFilterPlate(e.target.value.toUpperCase())}
              placeholder="ABC1234"
              maxLength={10}
              className="mt-1 block rounded-md border border-slate-200 px-3 py-2 text-sm font-mono uppercase focus:outline-none focus:ring-2 focus:ring-slate-200 w-32"
            />
          </label>

          <div className="text-sm">
            <DateInput
              label={t("payments.warranty.filterMonth")}
              type="month"
              value={filterMonth}
              onChange={(e) => setFilterMonth(e.target.value)}
            />
          </div>

          <div className="flex gap-2 pb-px">
            <button
              type="button"
              onClick={fetchPagamentos}
              className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
            >
              {t("actions.applyFilters")}
            </button>
            {(filterPlate || filterMonth) ? (
              <button
                type="button"
                onClick={() => { setFilterPlate(""); setFilterMonth(""); }}
                className="rounded-md border border-slate-200 px-4 py-2 text-sm"
              >
                {t("actions.clearFilters")}
              </button>
            ) : null}
          </div>
        </div>

        <button
          type="button"
          onClick={() => { setEditItem(null); setShowForm(true); }}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
        >
          + {t("payments.warranty.add")}
        </button>
      </div>

      {/* Total banner */}
      {pagamentos.length > 0 ? (
        <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 flex items-center justify-between">
          <span className="text-sm text-amber-800 font-medium">{t("payments.totalLabel")}</span>
          <span className="text-sm font-semibold text-amber-900">{formatMoney(total)}</span>
        </div>
      ) : null}

      {/* Table */}
      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        {loading ? (
          <div className="py-10 text-center text-sm text-slate-400">{t("common.loading")}</div>
        ) : pagamentos.length === 0 ? (
          <div className="py-10 text-center text-sm text-slate-400">{t("payments.empty")}</div>
        ) : (
          <table className="min-w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">{t("payments.warranty.plate")}</th>
                <th className="px-4 py-3">{t("payments.table.description")}</th>
                <th className="px-4 py-3 text-right">{t("payments.table.amount")}</th>
                <th className="px-4 py-3">{t("payments.table.date")}</th>
                <th className="px-4 py-3">{t("payments.table.notes")}</th>
                <th className="px-4 py-3 text-center">{t("payments.table.docs")}</th>
                <th className="px-4 py-3">{t("vehicles.table.actions")}</th>
              </tr>
            </thead>
            <tbody>
              {pagamentos.map((p) => (
                <tr key={p.id} className="border-t hover:bg-slate-50">
                  <td className="px-4 py-3 font-mono font-medium">
                    {p.veiculoPlaca ?? "—"}
                  </td>
                  <td className="px-4 py-3 text-slate-600">{p.descricao ?? "—"}</td>
                  <td className="px-4 py-3 text-right font-medium text-red-700">
                    {formatMoney(p.valor)}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap">{formatDate(p.dataPagamento)}</td>
                  <td className="px-4 py-3 text-slate-500 max-w-[140px] truncate">
                    {p.observacoes ?? "—"}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <button
                      type="button"
                      onClick={() => setDocsItem(p)}
                      className="rounded-md border border-slate-200 px-2 py-1 text-xs hover:bg-slate-50"
                    >
                      📎 {t("payments.documents.manage")}
                    </button>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => { setEditItem(p); setShowForm(true); }}
                        className="text-xs text-slate-600 hover:underline"
                      >
                        {t("actions.edit")}
                      </button>
                      {confirmDeleteId === p.id ? (
                        <div className="flex gap-1">
                          <button
                            type="button"
                            onClick={() => handleDelete(p.id)}
                            disabled={deletingId === p.id}
                            className="text-xs text-red-600 hover:underline disabled:opacity-50"
                          >
                            {t("actions.delete")}
                          </button>
                          <button
                            type="button"
                            onClick={() => setConfirmDeleteId(null)}
                            className="text-xs text-slate-400 hover:underline"
                          >
                            {t("actions.cancel")}
                          </button>
                        </div>
                      ) : (
                        <button
                          type="button"
                          onClick={() => setConfirmDeleteId(p.id)}
                          className="text-xs text-red-500 hover:underline"
                        >
                          {t("actions.delete")}
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showForm ? (
        <PagamentoFormModal
          mode={editItem ? "edit" : "create"}
          defaultType="GARANTIA"
          allowedTypes={["GARANTIA"]}
          item={editItem}
          onClose={() => setShowForm(false)}
          onSaved={() => { setShowForm(false); fetchPagamentos(); }}
        />
      ) : null}

      {docsItem ? (
        <DocumentosPagamentoModal
          pagamentoId={docsItem.id}
          pagamentoLabel={`${docsItem.veiculoPlaca ?? ""} · ${docsItem.descricao ?? ""} · ${formatMoney(docsItem.valor)}`}
          onClose={() => setDocsItem(null)}
        />
      ) : null}
    </div>
  );
}
