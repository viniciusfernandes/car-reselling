import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { TipoPagamento } from "../../../service/types";
import type { PagamentoItem } from "../../../service/types";
import { pagamentosApi, api, extractErrorMessage } from "../../../service/api";
import { parseMoney } from "../../../service/formatters";
import { useToast } from "../../../component/notification/ToastProvider";
import MoneyInput from "../../../component/input/MoneyInput";
import DateInput from "../../../component/input/DateInput";
import SelectInput from "../../../component/input/SelectInput";
import TextInput from "../../../component/input/TextInput";
import ComboboxInput from "../../../component/input/ComboboxInput";
import type { ApiResponse, ListaVeiculosResponse } from "../../../service/types";

type Props = {
  mode: "create" | "edit";
  defaultType?: TipoPagamento;
  allowedTypes?: TipoPagamento[];
  item?: PagamentoItem | null;
  onClose: () => void;
  onSaved: () => void;
};

const today = () => new Date().toISOString().slice(0, 10);

export default function PagamentoFormModal({
  mode,
  defaultType,
  allowedTypes,
  item,
  onClose,
  onSaved,
}: Props) {
  const { t } = useTranslation();
  const { showToast } = useToast();
  const [saving, setSaving] = useState(false);

  const [tipoPagamento, setTipoPagamento] = useState<TipoPagamento>(
    item?.tipoPagamento ?? defaultType ?? TipoPagamento.CUSTO_OPERACIONAL
  );
  const [descricao, setDescricao] = useState(item?.descricao ?? "");
  const [descricaoSuggestions, setDescricaoSuggestions] = useState<string[]>([]);
  const [valorStr, setValorStr] = useState(
    item?.valor != null ? String(item.valor.toFixed(2)).replace(".", ",") : ""
  );
  const [dataPagamento, setDataPagamento] = useState(item?.dataPagamento ?? today());
  const [veiculoPlate, setVeiculoPlate] = useState(item?.veiculoPlaca ?? "");
  const [veiculoLookupInfo, setVeiculoLookupInfo] = useState<string | null>(
    item?.veiculoPlaca ? item.veiculoPlaca : null
  );
  const [veiculoLookupNotVendido, setVeiculoLookupNotVendido] = useState(false);
  const [veiculoLookupError, setVeiculoLookupError] = useState<string | null>(null);
  const [lookingUp, setLookingUp] = useState(false);
  const [observacoes, setObservacoes] = useState(item?.observacoes ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});

  const dialogRef = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    dialogRef.current?.showModal();
  }, []);

  // Fetch description suggestions when paymentType changes
  useEffect(() => {
    pagamentosApi
      .listarDescricoes(tipoPagamento)
      .then((res) => setDescricaoSuggestions(res.data.data))
      .catch(() => setDescricaoSuggestions([]));
  }, [tipoPagamento]);

  const resetLookup = () => {
    setVeiculoLookupInfo(null);
    setVeiculoLookupNotVendido(false);
    setVeiculoLookupError(null);
  };

  const lookupVeiculo = async (plate: string) => {
    const trimmed = plate.trim().toUpperCase();
    if (!trimmed) {
      resetLookup();
      return;
    }
    try {
      setLookingUp(true);
      resetLookup();
      // Search WITHOUT status filter — let the backend enforce the SOLD rule.
      // Using size=10 to increase the chance of finding an exact plate match.
      const res = await api.get<ApiResponse<ListaVeiculosResponse>>("/veiculos", {
        params: { q: trimmed, size: 10 },
      });
      const found = res.data.data.items.find(
        (v) => v.placa.toUpperCase() === trimmed
      );
      if (found) {
        const label = `${found.placa} — ${found.marca} ${found.modelo} ${found.ano}`;
        setVeiculoLookupInfo(label);
        if (found.status !== "VENDIDO") {
          setVeiculoLookupNotVendido(true);
        }
      } else {
        setVeiculoLookupError(t("payments.form.vehicleNotFound"));
      }
    } catch {
      setVeiculoLookupError(t("payments.form.vehicleNotFound"));
    } finally {
      setLookingUp(false);
    }
  };

  const validate = () => {
    const errs: Record<string, string> = {};
    const valor = parseMoney(valorStr);
    if (!valorStr || isNaN(valor) || valor <= 0) {
      errs.amount = t("validation.required");
    }
    if (!dataPagamento) errs.paymentDate = t("validation.required");
    if (tipoPagamento === TipoPagamento.GARANTIA) {
      if (!veiculoPlate.trim()) {
        errs.vehiclePlate = t("validation.required");
      } else if (!veiculoLookupInfo && veiculoLookupError) {
        // Not found — block submission
        errs.vehiclePlate = veiculoLookupError;
      } else if (!veiculoLookupInfo && !lookingUp) {
        // User hasn't triggered lookup yet (typed but didn't blur)
        errs.vehiclePlate = t("payments.form.vehicleNotFound");
      }
      // vehicleLookupInfo + vehicleLookupNotSold → allow, backend will validate
    }
    return errs;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    const valor = parseMoney(valorStr);
    const payload = {
      tipoPagamento,
      descricao: descricao ? descricao.toUpperCase().trim() : undefined,
      valor,
      dataPagamento,
      veiculoPlaca:
        tipoPagamento === TipoPagamento.GARANTIA && veiculoPlate.trim()
          ? veiculoPlate.trim().toUpperCase()
          : undefined,
      observacoes: observacoes || undefined,
    };

    try {
      setSaving(true);
      if (mode === "create") {
        await pagamentosApi.create(payload);
        showToast(t("payments.form.created"), "success");
      } else if (item) {
        await pagamentosApi.update(item.id, payload);
        showToast(t("payments.form.updated"), "success");
      }
      onSaved();
      dialogRef.current?.close();
    } catch (error) {
      showToast(extractErrorMessage(error), "error");
    } finally {
      setSaving(false);
    }
  };

  const typeOptions: { value: TipoPagamento; labelKey: string }[] = [
    { value: TipoPagamento.GARANTIA, labelKey: "payments.types.GARANTIA" },
    { value: TipoPagamento.CUSTO_OPERACIONAL, labelKey: "payments.types.CUSTO_OPERACIONAL" },
    { value: TipoPagamento.PRO_LABORE, labelKey: "payments.types.PRO_LABORE" },
    { value: TipoPagamento.BONUS_PLR, labelKey: "payments.types.BONUS_PLR" },
    { value: TipoPagamento.OUTRO, labelKey: "payments.types.OUTRO" },
  ];

  const visibleTypes = allowedTypes
    ? typeOptions.filter((o) => allowedTypes.includes(o.value))
    : typeOptions;

  const typeFieldDisabled = !visibleTypes.some(
    (o) => o.value === TipoPagamento.GARANTIA || o.value === TipoPagamento.CUSTO_OPERACIONAL
  );

  return (
    <dialog
      ref={dialogRef}
      onClose={onClose}
      className="w-full max-w-lg rounded-xl shadow-xl backdrop:bg-black/40 p-0 border-0"
    >
      <form onSubmit={handleSubmit} noValidate>
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <h3 className="font-semibold text-slate-800">
            {mode === "create" ? t("payments.form.titleCreate") : t("payments.form.titleEdit")}
          </h3>
          <button
            type="button"
            onClick={() => dialogRef.current?.close()}
            className="text-slate-400 hover:text-slate-600 text-xl leading-none"
            aria-label={t("actions.cancel")}
          >
            ×
          </button>
        </div>

        <div className="space-y-4 px-6 py-5">
          {/* Payment category selector */}
          {visibleTypes.length > 1 ? (
            <SelectInput
              label={t("payments.form.category")}
              required
              value={tipoPagamento}
              options={visibleTypes.map((o) => ({ value: o.value, label: t(o.labelKey) }))}
              onChange={(e) => {
                setTipoPagamento(e.target.value as TipoPagamento);
                setDescricao("");
              }}
              error={errors.paymentType}
            />
          ) : (
            <div className="text-sm">
              <span className="font-medium text-slate-700">{t("payments.form.category")}</span>
              <div className="mt-1 rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600">
                {t(`payments.types.${visibleTypes[0]?.value ?? tipoPagamento}`)}
              </div>
            </div>
          )}

          {/* Vehicle plate for WARRANTY — plain text input with lookup */}
          {tipoPagamento === TipoPagamento.GARANTIA ? (
            <div>
              <label className="block text-sm">
                <span className="font-medium text-slate-700">
                  {t("payments.form.vehicle")} <span className="text-red-500">*</span>
                </span>
                <div className="relative mt-1">
                  <input
                    type="text"
                    value={veiculoPlate}
                    onChange={(e) => {
                      const val = e.target.value.toUpperCase();
                      setVeiculoPlate(val);
                      resetLookup();
                    }}
                    onBlur={() => lookupVeiculo(veiculoPlate)}
                    placeholder={t("payments.form.vehiclePlatePlaceholder")}
                    maxLength={10}
                    className={`w-full rounded-md border px-3 py-2 pr-10 text-sm font-mono uppercase shadow-sm focus:outline-none focus:ring-2 ${
                      errors.vehiclePlate || veiculoLookupError
                        ? "border-red-400 focus:ring-red-200"
                        : veiculoLookupInfo && veiculoLookupNotVendido
                          ? "border-amber-400 focus:ring-amber-200"
                          : veiculoLookupInfo
                            ? "border-green-400 focus:ring-green-200"
                            : "border-slate-200 focus:ring-slate-200"
                    }`}
                  />
                  <span className="absolute right-3 top-2.5">
                    {lookingUp ? (
                      <span className="text-xs text-slate-400">…</span>
                    ) : veiculoLookupInfo && !veiculoLookupNotVendido ? (
                      <span className="text-green-600 text-sm">✓</span>
                    ) : veiculoLookupInfo && veiculoLookupNotVendido ? (
                      <span className="text-amber-500 text-sm">⚠</span>
                    ) : veiculoLookupError ? (
                      <span className="text-red-500 text-sm">✗</span>
                    ) : null}
                  </span>
                </div>
              </label>

              {/* Found + SOLD → green */}
              {veiculoLookupInfo && !veiculoLookupNotVendido ? (
                <div className="mt-1 rounded-md bg-green-50 border border-green-200 px-3 py-1.5 text-xs text-green-800">
                  ✓ {veiculoLookupInfo}
                </div>
              ) : null}

              {/* Found + NOT SOLD → orange warning */}
              {veiculoLookupInfo && veiculoLookupNotVendido ? (
                <div className="mt-1 rounded-md bg-amber-50 border border-amber-200 px-3 py-2 text-xs text-amber-800">
                  <div className="font-medium">{veiculoLookupInfo}</div>
                  <div className="mt-0.5">{t("payments.form.vehicleNotSold")}</div>
                </div>
              ) : null}

              {/* Not found → red error */}
              {veiculoLookupError ? (
                <p className="mt-1 text-xs text-red-600">{veiculoLookupError}</p>
              ) : null}

              {/* Submit attempt without lookup → required error */}
              {errors.vehiclePlate && !veiculoLookupError && !veiculoLookupInfo ? (
                <p className="mt-1 text-xs text-red-600">{errors.vehiclePlate}</p>
              ) : null}
            </div>
          ) : null}

          {/* Tipo (description) — ComboboxInput with UPPERCASE */}

          {!typeFieldDisabled && <ComboboxInput
            label={t("payments.form.description")}
            placeholder={t("payments.form.descriptionPlaceholder")}
            value={descricao}
            suggestions={descricaoSuggestions}
            onChange={(e) => setDescricao(e.target.value.toUpperCase())}
            onBlur={() => setDescricao((prev) => prev.toUpperCase().trim())}
          />}

          <div className="grid grid-cols-2 gap-4">
            <MoneyInput
              label={`${t("payments.form.amount")} *`}
              required
              value={valorStr}
              onValueChange={setValorStr}
              error={errors.amount}
            />
            <DateInput
              label={`${t("payments.form.date")} *`}
              type="date"
              required
              value={dataPagamento}
              onChange={(e) => setDataPagamento(e.target.value)}
              error={errors.paymentDate}
            />
          </div>

          <label className="block text-sm">
            <span className="font-medium text-slate-700">{t("payments.form.notes")}</span>
            <textarea
              value={observacoes}
              onChange={(e) => setObservacoes(e.target.value)}
              rows={2}
              placeholder={t("payments.form.notesPlaceholder")}
              className="mt-1 w-full rounded-md border border-slate-200 px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-200 resize-none"
            />
          </label>
        </div>

        <div className="flex justify-end gap-2 border-t border-slate-200 px-6 py-4">
          <button
            type="button"
            onClick={() => dialogRef.current?.close()}
            className="rounded-md border border-slate-200 px-4 py-2 text-sm hover:bg-slate-50"
          >
            {t("actions.cancel")}
          </button>
          <button
            type="submit"
            disabled={saving}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white disabled:opacity-60"
          >
            {saving ? t("actions.saving") : t("actions.save")}
          </button>
        </div>
      </form>
    </dialog>
  );
}
