import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import TextInput from "../../component/input/TextInput";
import MoneyInput from "../../component/input/MoneyInput";
import { useToast } from "../../component/notification/ToastProvider";
import {
  fetchPartners,
  fetchPartner,
  updatePartner,
  fetchPartnerHistory,
  createPartner,
  disablePartner,
} from "../../service/partners";
import { extractErrorMessage } from "../../service/api";
import { formatNumber, parseMoney } from "../../service/formatters";
import type { PartnerItem, PartnerHistoryItem } from "../../service/types";

type EditFormState = {
  name: string;
  city: string;
  phone: string;
  email: string;
  commissionRate: string;
};

const EMPTY_FORM: EditFormState = {
  name: "",
  city: "",
  phone: "",
  email: "",
  commissionRate: "",
};

export default function PartnerManagementPage() {
  const { t, i18n } = useTranslation();
  const { showToast } = useToast();

  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [createMode, setCreateMode] = useState(false);
  const [form, setForm] = useState<EditFormState>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const [history, setHistory] = useState<PartnerHistoryItem[]>([]);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    fetchPartners()
      .then((res) => setPartners(res.partners))
      .catch((err) => showToast(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
  }, []);

  function handleSelect(partner: PartnerItem) {
    setSelectedId(partner.id);
    setCreateMode(false);
    setHistoryOpen(false);
    setHistory([]);
    setLoading(true);
    fetchPartner(partner.id)
      .then((detail) => {
        setForm({
          name: detail.name ?? "",
          city: detail.city ?? "",
          phone: detail.phone ?? "",
          email: detail.email ?? "",
          commissionRate: detail.commissionRate != null
            ? formatNumber(detail.commissionRate)
            : "",
        });
      })
      .catch((err) => showToast(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
  }

  function handleCancel() {
    setSelectedId(null);
    setCreateMode(false);
    setForm(EMPTY_FORM);
    setHistory([]);
    setHistoryOpen(false);
  }

  async function handleSave() {
    if (!selectedId) return;
    if (!form.name.trim()) {
      showToast(t("partners.errors.nameRequired"), "error");
      return;
    }
    const rawRate = form.commissionRate
      ? parseMoney(form.commissionRate)
      : null;
    if (form.commissionRate && (Number.isNaN(rawRate) || (rawRate !== null && (rawRate < 0 || rawRate > 100)))) {
      showToast(t("partners.errors.invalidRate"), "error");
      return;
    }
    setSaving(true);
    try {
      await updatePartner(selectedId, {
        name: form.name.trim(),
        city: form.city.trim() || undefined,
        phone: form.phone.trim() || undefined,
        email: form.email.trim() || undefined,
        commissionRate: rawRate ?? undefined,
      });
      setPartners((prev) =>
        prev.map((p) =>
          p.id === selectedId
            ? { ...p, name: form.name.trim(), city: form.city.trim() || null,
                phone: form.phone.trim() || null, email: form.email.trim() || null,
                commissionRate: rawRate ?? null }
            : p
        )
      );
      showToast(t("partners.savedSuccess"), "success");
      if (historyOpen) {
        loadHistory(selectedId);
      }
    } catch (err) {
      showToast(extractErrorMessage(err), "error");
    } finally {
      setSaving(false);
    }
  }

  function loadHistory(id: string) {
    setHistoryLoading(true);
    fetchPartnerHistory(id)
      .then((res) => setHistory(res.history))
      .catch((err) => showToast(extractErrorMessage(err), "error"))
      .finally(() => setHistoryLoading(false));
  }

  function handleToggleHistory() {
    if (!selectedId) return;
    if (!historyOpen) {
      setHistoryOpen(true);
      loadHistory(selectedId);
    } else {
      setHistoryOpen(false);
    }
  }

  const formatDate = (value: string) => {
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return value;
    return d.toLocaleString(i18n.language);
  };

  async function handleCreate() {
    if (!form.name.trim()) {
      showToast(t("partners.errors.nameRequired"), "error");
      return;
    }
    const rawRate = form.commissionRate
      ? parseMoney(form.commissionRate)
      : null;
    if (form.commissionRate && (Number.isNaN(rawRate) || (rawRate !== null && (rawRate < 0 || rawRate > 100)))) {
      showToast(t("partners.errors.invalidRate"), "error");
      return;
    }
    setSaving(true);
    try {
      const res = await createPartner({
        name: form.name.trim(),
        city: form.city.trim() || undefined,
        phone: form.phone.trim() || undefined,
        email: form.email.trim() || undefined,
        commissionRate: rawRate ?? undefined,
      });
      showToast(t("partners.createdSuccess"), "success");
      setCreateMode(false);
      setForm(EMPTY_FORM);
      const list = await fetchPartners();
      setPartners(list.partners);
    } catch (err) {
      showToast(extractErrorMessage(err), "error");
    } finally {
      setSaving(false);
    }
  }

  async function handleDisable() {
    if (!selectedId) return;
    if (!window.confirm(t("partners.disableConfirm"))) return;
    setSaving(true);
    try {
      await disablePartner(selectedId);
      showToast(t("partners.disabledSuccess"), "success");
      setPartners((prev) => prev.filter((p) => p.id !== selectedId));
      setSelectedId(null);
      setForm(EMPTY_FORM);
    } catch (err) {
      showToast(extractErrorMessage(err), "error");
    } finally {
      setSaving(false);
    }
  }

  function handleNewPartner() {
    setSelectedId(null);
    setCreateMode(true);
    setForm(EMPTY_FORM);
  }

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-slate-800">{t("partners.title")}</h2>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Partner list */}
        <div className="lg:col-span-1">
          <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
            <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
              <h3 className="text-sm font-semibold text-slate-700">
                {t("partners.list")}
              </h3>
              <button
                type="button"
                onClick={handleNewPartner}
                className="rounded-md bg-slate-800 px-3 py-1.5 text-xs font-medium text-white hover:bg-slate-700"
              >
                {t("partners.newPartner")}
              </button>
            </div>
            {loading && !selectedId ? (
              <p className="px-4 py-6 text-sm text-slate-400">
                {t("common.loading")}
              </p>
            ) : partners.length === 0 ? (
              <p className="px-4 py-6 text-sm text-slate-400">
                {t("partners.empty")}
              </p>
            ) : (
              <ul className="divide-y divide-slate-100">
                {partners.map((p) => (
                  <li key={p.id}>
                    <button
                      type="button"
                      onClick={() => handleSelect(p)}
                      className={`w-full px-4 py-3 text-left transition-colors hover:bg-slate-50 ${
                        selectedId === p.id
                          ? "bg-blue-50 border-l-4 border-blue-500"
                          : ""
                      }`}
                    >
                      <p className="text-sm font-medium text-slate-800">{p.name}</p>
                      {p.city ? (
                        <p className="text-xs text-slate-500">{p.city}</p>
                      ) : null}
                      {p.commissionRate != null ? (
                        <p className="text-xs text-slate-500">
                          {t("partners.commissionRate")}:{" "}
                          {formatNumber(p.commissionRate)}%
                        </p>
                      ) : null}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* Edit / Create form */}
        <div className="lg:col-span-2">
          {selectedId || createMode ? (
            <div className="space-y-4 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
              <h3 className="text-sm font-semibold text-slate-700">
                {createMode ? t("partners.createTitle") : t("partners.editTitle")}
              </h3>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <TextInput
                  label={t("partners.fields.name")}
                  value={form.name}
                  required
                  onChange={(e) =>
                    setForm((f) => ({ ...f, name: e.target.value }))
                  }
                />
                <TextInput
                  label={t("partners.fields.city")}
                  value={form.city}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, city: e.target.value }))
                  }
                />
                <TextInput
                  label={t("partners.fields.phone")}
                  value={form.phone}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, phone: e.target.value }))
                  }
                />
                <TextInput
                  label={t("partners.fields.email")}
                  type="email"
                  value={form.email}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, email: e.target.value }))
                  }
                />
                <MoneyInput
                  label={t("partners.fields.commissionRate")}
                  value={form.commissionRate}
                  onValueChange={(v) =>
                    setForm((f) => ({ ...f, commissionRate: v }))
                  }
                />
              </div>

              <div className="flex flex-wrap items-center gap-3 pt-2">
                {createMode ? (
                  <>
                    <button
                      type="button"
                      onClick={handleCreate}
                      disabled={saving}
                      className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 disabled:opacity-50"
                    >
                      {saving ? t("common.saving") : t("partners.create")}
                    </button>
                    <button
                      type="button"
                      onClick={handleCancel}
                      className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                    >
                      {t("common.cancel")}
                    </button>
                  </>
                ) : (
                  <>
                    <button
                      type="button"
                      onClick={handleSave}
                      disabled={saving}
                      className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 disabled:opacity-50"
                    >
                      {saving ? t("common.saving") : t("common.save")}
                    </button>
                    <button
                      type="button"
                      onClick={handleCancel}
                      className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                    >
                      {t("common.cancel")}
                    </button>
                    <button
                      type="button"
                      onClick={handleDisable}
                      disabled={saving}
                      className="rounded-md border border-red-300 bg-white px-4 py-2 text-sm font-medium text-red-700 hover:bg-red-50 disabled:opacity-50"
                    >
                      {t("partners.disable")}
                    </button>
                    <button
                      type="button"
                      onClick={handleToggleHistory}
                      className="ml-auto rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                    >
                      {historyOpen
                        ? t("partners.hideHistory")
                        : t("partners.showHistory")}
                    </button>
                  </>
                )}
              </div>

              {/* Change history — only for existing partners */}
              {!createMode && historyOpen ? (
                <div className="mt-4 border-t border-slate-100 pt-4">
                  <h4 className="mb-3 text-sm font-semibold text-slate-700">
                    {t("partners.historyTitle")}
                  </h4>
                  {historyLoading ? (
                    <p className="text-sm text-slate-400">{t("common.loading")}</p>
                  ) : history.length === 0 ? (
                    <p className="text-sm text-slate-400">
                      {t("partners.historyEmpty")}
                    </p>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full text-xs text-slate-700">
                        <thead>
                          <tr className="border-b border-slate-100 text-left text-slate-500">
                            <th className="pb-2 pr-3 font-medium">
                              {t("partners.historyColumns.changedAt")}
                            </th>
                            <th className="pb-2 pr-3 font-medium">
                              {t("partners.historyColumns.changedBy")}
                            </th>
                            <th className="pb-2 pr-3 font-medium">
                              {t("partners.fields.name")}
                            </th>
                            <th className="pb-2 pr-3 font-medium">
                              {t("partners.fields.city")}
                            </th>
                            <th className="pb-2 pr-3 font-medium">
                              {t("partners.fields.phone")}
                            </th>
                            <th className="pb-2 pr-3 font-medium">
                              {t("partners.fields.email")}
                            </th>
                            <th className="pb-2 font-medium">
                              {t("partners.fields.commissionRate")}
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          {history.map((h) => (
                            <tr
                              key={h.id}
                              className="border-b border-slate-50 hover:bg-slate-50"
                            >
                              <td className="py-2 pr-3 whitespace-nowrap">
                                {formatDate(h.changedAt)}
                              </td>
                              <td className="py-2 pr-3">{h.changedBy ?? "-"}</td>
                              <td className="py-2 pr-3">{h.name}</td>
                              <td className="py-2 pr-3">{h.city ?? "-"}</td>
                              <td className="py-2 pr-3">{h.phone ?? "-"}</td>
                              <td className="py-2 pr-3">{h.email ?? "-"}</td>
                              <td className="py-2">
                                {h.commissionRate != null
                                  ? `${formatNumber(h.commissionRate)}`
                                  : "-"}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              ) : null}
            </div>
          ) : (
            <div className="flex h-full items-center justify-center rounded-lg border border-dashed border-slate-200 bg-white p-12 text-center">
              <p className="text-sm text-slate-400">{t("partners.selectHint")}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
