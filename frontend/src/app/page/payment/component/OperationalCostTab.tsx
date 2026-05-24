import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type { PaymentItem } from "../../../service/types";
import { paymentsApi, extractErrorMessage } from "../../../service/api";
import { formatMoney, formatDate } from "../../../service/formatters";
import { useToast } from "../../../component/notification/ToastProvider";
import DateInput from "../../../component/input/DateInput";
import PaymentFormModal from "./PaymentFormModal";

const currentMonth = () => new Date().toISOString().slice(0, 7);

export default function OperationalCostTab() {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [payments, setPayments] = useState<PaymentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editItem, setEditItem] = useState<PaymentItem | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);
  const [filterMonth, setFilterMonth] = useState(currentMonth());

  const fetchPayments = useCallback(async () => {
    try {
      setLoading(true);
      const res = await paymentsApi.list({
        paymentType: "OPERATIONAL_COST",
        referenceMonth: filterMonth || undefined,
      });
      setPayments(res.data.data.payments);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setLoading(false);
    }
  }, [filterMonth, showToast]);

  useEffect(() => {
    fetchPayments();
  }, [fetchPayments]);

  const handleDelete = async (id: string) => {
    try {
      setDeletingId(id);
      await paymentsApi.delete(id);
      showToast(t("payments.deleted"), "success");
      setPayments((prev) => prev.filter((p) => p.id !== id));
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setDeletingId(null);
      setConfirmDeleteId(null);
    }
  };

  const total = payments.reduce((sum, p) => sum + p.amount, 0);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-3 items-end">
          <DateInput
            label={t("payments.operational.filterMonth")}
            type="month"
            value={filterMonth}
            onChange={(e) => setFilterMonth(e.target.value)}
          />
          <button
            type="button"
            onClick={fetchPayments}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
          >
            {t("actions.applyFilters")}
          </button>
          {filterMonth ? (
            <button
              type="button"
              onClick={() => setFilterMonth("")}
              className="rounded-md border border-slate-200 px-4 py-2 text-sm"
            >
              {t("actions.clearFilters")}
            </button>
          ) : null}
        </div>
        <button
          type="button"
          onClick={() => { setEditItem(null); setShowForm(true); }}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
        >
          + {t("payments.operational.add")}
        </button>
      </div>

      {payments.length > 0 ? (
        <div className="rounded-lg border border-orange-200 bg-orange-50 px-4 py-3 flex items-center justify-between">
          <span className="text-sm text-orange-800 font-medium">
            {filterMonth
              ? t("payments.operational.totalMonth", { month: filterMonth })
              : t("payments.totalLabel")}
          </span>
          <span className="text-sm font-semibold text-orange-900">{formatMoney(total)}</span>
        </div>
      ) : null}

      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        {loading ? (
          <div className="py-10 text-center text-sm text-slate-400">{t("common.loading")}</div>
        ) : payments.length === 0 ? (
          <div className="py-10 text-center text-sm text-slate-400">{t("payments.empty")}</div>
        ) : (
          <table className="min-w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">{t("payments.table.description")}</th>
                <th className="px-4 py-3 text-right">{t("payments.table.amount")}</th>
                <th className="px-4 py-3">{t("payments.table.date")}</th>
                <th className="px-4 py-3">{t("payments.table.referenceMonth")}</th>
                <th className="px-4 py-3">{t("payments.table.notes")}</th>
                <th className="px-4 py-3">{t("vehicles.table.actions")}</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.id} className="border-t hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-700">
                    {p.description ?? "—"}
                  </td>
                  <td className="px-4 py-3 text-right font-medium text-red-700">
                    {formatMoney(p.amount)}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap">{formatDate(p.paymentDate)}</td>
                  <td className="px-4 py-3">{p.referenceMonth ?? "—"}</td>
                  <td className="px-4 py-3 text-slate-500 max-w-[200px] truncate">
                    {p.notes ?? "—"}
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
        <PaymentFormModal
          mode={editItem ? "edit" : "create"}
          defaultType="OPERATIONAL_COST"
          allowedTypes={["OPERATIONAL_COST"]}
          item={editItem}
          onClose={() => setShowForm(false)}
          onSaved={() => { setShowForm(false); fetchPayments(); }}
        />
      ) : null}
    </div>
  );
}
