import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PaymentType } from "../../../service/types";
import type { PaymentItem } from "../../../service/types";
import { paymentsApi, extractErrorMessage } from "../../../service/api";
import { formatMoney, formatDate } from "../../../service/formatters";
import { useToast } from "../../../component/notification/ToastProvider";
import DateInput from "../../../component/input/DateInput";
import PaymentFormModal from "./PaymentFormModal";

const ALLOWED_TYPES: PaymentType[] = [PaymentType.PRO_LABORE, PaymentType.BONUS_PLR, PaymentType.OTHER];

const currentMonth = () => new Date().toISOString().slice(0, 7);

export default function ProLaboreBonusTab() {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const [payments, setPayments] = useState<PaymentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editItem, setEditItem] = useState<PaymentItem | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);
  const [filterMonth, setFilterMonth] = useState(currentMonth());

  const fetchAll = useCallback(async () => {
    try {
      setLoading(true);
      const results = await Promise.all(
        ALLOWED_TYPES.map((type) =>
          paymentsApi.list({ paymentType: type, referenceMonth: filterMonth || undefined })
        )
      );
      const all = results.flatMap((r) => r.data.data.payments);
      all.sort(
        (a, b) =>
          new Date(b.paymentDate).getTime() - new Date(a.paymentDate).getTime()
      );
      setPayments(all);
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setLoading(false);
    }
  }, [filterMonth, showToast]);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

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

  const totalByType = (type: PaymentType) =>
    payments.filter((p) => p.paymentType === type).reduce((sum, p) => sum + p.amount, 0);

  const total = payments.reduce((sum, p) => sum + p.amount, 0);

  const typeBadgeClass: Record<PaymentType, string> = {
    [PaymentType.WARRANTY]: "bg-amber-100 text-amber-800",
    [PaymentType.OPERATIONAL_COST]: "bg-orange-100 text-orange-800",
    [PaymentType.PRO_LABORE]: "bg-blue-100 text-blue-800",
    [PaymentType.BONUS_PLR]: "bg-green-100 text-green-800",
    [PaymentType.OTHER]: "bg-slate-100 text-slate-700",
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-3 items-end">
          <DateInput
            label={t("payments.bonus.filterMonth")}
            type="month"
            value={filterMonth}
            onChange={(e) => setFilterMonth(e.target.value)}
          />
          <button
            type="button"
            onClick={fetchAll}
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
          + {t("payments.bonus.add")}
        </button>
      </div>

      {payments.length > 0 ? (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div className="rounded-lg border border-blue-200 bg-blue-50 px-4 py-3">
            <div className="text-xs text-blue-700 font-medium">{t("payments.types.PRO_LABORE")}</div>
            <div className="text-base font-semibold text-blue-900">{formatMoney(totalByType(PaymentType.PRO_LABORE))}</div>
          </div>
          <div className="rounded-lg border border-green-200 bg-green-50 px-4 py-3">
            <div className="text-xs text-green-700 font-medium">{t("payments.types.BONUS_PLR")}</div>
            <div className="text-base font-semibold text-green-900">{formatMoney(totalByType(PaymentType.BONUS_PLR))}</div>
          </div>
          <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
            <div className="text-xs text-slate-600 font-medium">{t("payments.types.OTHER")}</div>
            <div className="text-base font-semibold text-slate-800">{formatMoney(totalByType(PaymentType.OTHER))}</div>
          </div>
          <div className="rounded-lg border border-slate-300 bg-white px-4 py-3">
            <div className="text-xs text-slate-500 font-medium">{t("payments.totalLabel")}</div>
            <div className="text-base font-semibold text-slate-900">{formatMoney(total)}</div>
          </div>
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
                <th className="px-4 py-3">{t("payments.table.type")}</th>
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
                  <td className="px-4 py-3">
                    <span
                      className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${typeBadgeClass[p.paymentType]}`}
                    >
                      {t(`payments.types.${p.paymentType}`)}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-600">{p.description ?? "—"}</td>
                  <td className="px-4 py-3 text-right font-medium text-slate-800">
                    {formatMoney(p.amount)}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap">{formatDate(p.paymentDate)}</td>
                  <td className="px-4 py-3">{p.referenceMonth ?? "—"}</td>
                  <td className="px-4 py-3 text-slate-500 max-w-[180px] truncate">
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
          defaultType={editItem?.paymentType ?? PaymentType.PRO_LABORE}
          allowedTypes={ALLOWED_TYPES}
          item={editItem}
          onClose={() => setShowForm(false)}
          onSaved={() => { setShowForm(false); fetchAll(); }}
        />
      ) : null}
    </div>
  );
}
