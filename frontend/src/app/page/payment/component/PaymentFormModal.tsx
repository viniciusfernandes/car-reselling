import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { PaymentType } from "../../../service/types";
import type { PaymentItem } from "../../../service/types";
import { paymentsApi, api, extractErrorMessage } from "../../../service/api";
import { parseMoney } from "../../../service/formatters";
import { useToast } from "../../../component/notification/ToastProvider";
import MoneyInput from "../../../component/input/MoneyInput";
import DateInput from "../../../component/input/DateInput";
import SelectInput from "../../../component/input/SelectInput";
import TextInput from "../../../component/input/TextInput";
import ComboboxInput from "../../../component/input/ComboboxInput";
import type { ApiResponse, VehicleListResponse } from "../../../service/types";

type Props = {
  mode: "create" | "edit";
  defaultType?: PaymentType;
  allowedTypes?: PaymentType[];
  item?: PaymentItem | null;
  onClose: () => void;
  onSaved: () => void;
};

const today = () => new Date().toISOString().slice(0, 10);
const currentMonth = () => new Date().toISOString().slice(0, 7);

export default function PaymentFormModal({
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

  const [paymentType, setPaymentType] = useState<PaymentType>(
    item?.paymentType ?? defaultType ?? PaymentType.OPERATIONAL_COST
  );
  const [description, setDescription] = useState(item?.description ?? "");
  const [descriptionSuggestions, setDescriptionSuggestions] = useState<string[]>([]);
  const [amountStr, setAmountStr] = useState(
    item?.amount != null ? String(item.amount.toFixed(2)).replace(".", ",") : ""
  );
  const [paymentDate, setPaymentDate] = useState(item?.paymentDate ?? today());
  const [vehiclePlate, setVehiclePlate] = useState(item?.vehicleLicensePlate ?? "");
  const [vehicleLookupInfo, setVehicleLookupInfo] = useState<string | null>(
    item?.vehicleLicensePlate ? item.vehicleLicensePlate : null
  );
  const [vehicleLookupNotSold, setVehicleLookupNotSold] = useState(false);
  const [vehicleLookupError, setVehicleLookupError] = useState<string | null>(null);
  const [lookingUp, setLookingUp] = useState(false);
  const [referenceMonth, setReferenceMonth] = useState(item?.referenceMonth ?? currentMonth());
  const [notes, setNotes] = useState(item?.notes ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});

  const dialogRef = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    dialogRef.current?.showModal();
  }, []);

  // Fetch description suggestions when paymentType changes
  useEffect(() => {
    paymentsApi
      .listDescriptions(paymentType)
      .then((res) => setDescriptionSuggestions(res.data.data))
      .catch(() => setDescriptionSuggestions([]));
  }, [paymentType]);

  const resetLookup = () => {
    setVehicleLookupInfo(null);
    setVehicleLookupNotSold(false);
    setVehicleLookupError(null);
  };

  const lookupVehicle = async (plate: string) => {
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
      const res = await api.get<ApiResponse<VehicleListResponse>>("/vehicles", {
        params: { q: trimmed, size: 10 },
      });
      const found = res.data.data.items.find(
        (v) => v.licensePlate.toUpperCase() === trimmed
      );
      if (found) {
        const label = `${found.licensePlate} — ${found.brand} ${found.model} ${found.year}`;
        setVehicleLookupInfo(label);
        if (found.status !== "SOLD") {
          setVehicleLookupNotSold(true);
        }
      } else {
        setVehicleLookupError(t("payments.form.vehicleNotFound"));
      }
    } catch {
      setVehicleLookupError(t("payments.form.vehicleNotFound"));
    } finally {
      setLookingUp(false);
    }
  };

  const validate = () => {
    const errs: Record<string, string> = {};
    const amount = parseMoney(amountStr);
    if (!amountStr || isNaN(amount) || amount <= 0) {
      errs.amount = t("validation.required");
    }
    if (!paymentDate) errs.paymentDate = t("validation.required");
    if (paymentType === PaymentType.WARRANTY) {
      if (!vehiclePlate.trim()) {
        errs.vehiclePlate = t("validation.required");
      } else if (!vehicleLookupInfo && vehicleLookupError) {
        // Not found — block submission
        errs.vehiclePlate = vehicleLookupError;
      } else if (!vehicleLookupInfo && !lookingUp) {
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

    const amount = parseMoney(amountStr);
    const payload = {
      paymentType,
      description: description ? description.toUpperCase().trim() : undefined,
      amount,
      paymentDate,
      vehicleLicensePlate:
        paymentType === PaymentType.WARRANTY && vehiclePlate.trim()
          ? vehiclePlate.trim().toUpperCase()
          : undefined,
      referenceMonth: referenceMonth || undefined,
      notes: notes || undefined,
    };

    try {
      setSaving(true);
      if (mode === "create") {
        await paymentsApi.create(payload);
        showToast(t("payments.form.created"), "success");
      } else if (item) {
        await paymentsApi.update(item.id, payload);
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

  const typeOptions: { value: PaymentType; labelKey: string }[] = [
    { value: PaymentType.WARRANTY, labelKey: "payments.types.WARRANTY" },
    { value: PaymentType.OPERATIONAL_COST, labelKey: "payments.types.OPERATIONAL_COST" },
    { value: PaymentType.PRO_LABORE, labelKey: "payments.types.PRO_LABORE" },
    { value: PaymentType.BONUS_PLR, labelKey: "payments.types.BONUS_PLR" },
    { value: PaymentType.OTHER, labelKey: "payments.types.OTHER" },
  ];

  const visibleTypes = allowedTypes
    ? typeOptions.filter((o) => allowedTypes.includes(o.value))
    : typeOptions;

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
              value={paymentType}
              options={visibleTypes.map((o) => ({ value: o.value, label: t(o.labelKey) }))}
              onChange={(e) => {
                setPaymentType(e.target.value as PaymentType);
                setDescription("");
              }}
              error={errors.paymentType}
            />
          ) : (
            <div className="text-sm">
              <span className="font-medium text-slate-700">{t("payments.form.category")}</span>
              <div className="mt-1 rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600">
                {t(`payments.types.${visibleTypes[0]?.value ?? paymentType}`)}
              </div>
            </div>
          )}

          {/* Vehicle plate for WARRANTY — plain text input with lookup */}
          {paymentType === PaymentType.WARRANTY ? (
            <div>
              <label className="block text-sm">
                <span className="font-medium text-slate-700">
                  {t("payments.form.vehicle")} <span className="text-red-500">*</span>
                </span>
                <div className="relative mt-1">
                  <input
                    type="text"
                    value={vehiclePlate}
                    onChange={(e) => {
                      const val = e.target.value.toUpperCase();
                      setVehiclePlate(val);
                      resetLookup();
                    }}
                    onBlur={() => lookupVehicle(vehiclePlate)}
                    placeholder={t("payments.form.vehiclePlatePlaceholder")}
                    maxLength={10}
                    className={`w-full rounded-md border px-3 py-2 pr-10 text-sm font-mono uppercase shadow-sm focus:outline-none focus:ring-2 ${
                      errors.vehiclePlate || vehicleLookupError
                        ? "border-red-400 focus:ring-red-200"
                        : vehicleLookupInfo && vehicleLookupNotSold
                          ? "border-amber-400 focus:ring-amber-200"
                          : vehicleLookupInfo
                            ? "border-green-400 focus:ring-green-200"
                            : "border-slate-200 focus:ring-slate-200"
                    }`}
                  />
                  <span className="absolute right-3 top-2.5">
                    {lookingUp ? (
                      <span className="text-xs text-slate-400">…</span>
                    ) : vehicleLookupInfo && !vehicleLookupNotSold ? (
                      <span className="text-green-600 text-sm">✓</span>
                    ) : vehicleLookupInfo && vehicleLookupNotSold ? (
                      <span className="text-amber-500 text-sm">⚠</span>
                    ) : vehicleLookupError ? (
                      <span className="text-red-500 text-sm">✗</span>
                    ) : null}
                  </span>
                </div>
              </label>

              {/* Found + SOLD → green */}
              {vehicleLookupInfo && !vehicleLookupNotSold ? (
                <div className="mt-1 rounded-md bg-green-50 border border-green-200 px-3 py-1.5 text-xs text-green-800">
                  ✓ {vehicleLookupInfo}
                </div>
              ) : null}

              {/* Found + NOT SOLD → orange warning */}
              {vehicleLookupInfo && vehicleLookupNotSold ? (
                <div className="mt-1 rounded-md bg-amber-50 border border-amber-200 px-3 py-2 text-xs text-amber-800">
                  <div className="font-medium">{vehicleLookupInfo}</div>
                  <div className="mt-0.5">{t("payments.form.vehicleNotSold")}</div>
                </div>
              ) : null}

              {/* Not found → red error */}
              {vehicleLookupError ? (
                <p className="mt-1 text-xs text-red-600">{vehicleLookupError}</p>
              ) : null}

              {/* Submit attempt without lookup → required error */}
              {errors.vehiclePlate && !vehicleLookupError && !vehicleLookupInfo ? (
                <p className="mt-1 text-xs text-red-600">{errors.vehiclePlate}</p>
              ) : null}
            </div>
          ) : null}

          {/* Tipo (description) — ComboboxInput with UPPERCASE */}
          <ComboboxInput
            label={t("payments.form.description")}
            placeholder={t("payments.form.descriptionPlaceholder")}
            value={description}
            suggestions={descriptionSuggestions}
            onChange={(e) => setDescription(e.target.value.toUpperCase())}
            onBlur={() => setDescription((prev) => prev.toUpperCase().trim())}
          />

          <div className="grid grid-cols-2 gap-4">
            <MoneyInput
              label={`${t("payments.form.amount")} *`}
              required
              value={amountStr}
              onValueChange={setAmountStr}
              error={errors.amount}
            />
            <DateInput
              label={`${t("payments.form.date")} *`}
              type="date"
              required
              value={paymentDate}
              onChange={(e) => setPaymentDate(e.target.value)}
              error={errors.paymentDate}
            />
          </div>

          {/* Reference month — calendar picker */}
          <DateInput
            label={t("payments.form.referenceMonth")}
            type="month"
            value={referenceMonth}
            onChange={(e) => setReferenceMonth(e.target.value)}
          />

          <label className="block text-sm">
            <span className="font-medium text-slate-700">{t("payments.form.notes")}</span>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
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
