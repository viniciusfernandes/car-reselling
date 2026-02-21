import { InputHTMLAttributes, useEffect, useMemo, useRef } from "react";
import { useTranslation } from "react-i18next";
import { normalizeMoney, formatNumber } from "../../service/formatters";

type Props = Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "value"> & {
  label: string;
  value: string;
  onValueChange: (value: string) => void;
  error?: string;
};

const normalizeMoneyInput = (value: string, locale: string) => {
  const sanitized = normalizeMoney(value, locale);
  const [integerPart, decimalPart = ""] = sanitized.split(".");
  const normalizedDecimal = decimalPart.slice(0, 2);
  return normalizedDecimal.length > 0
    ? `${integerPart}.${normalizedDecimal}`
    : integerPart;
};

const formatMoneyValue = (value: string) => {
  if (!value) return "";
  const numeric = Number(value);
  if (Number.isNaN(numeric)) return value;
  return formatNumber(numeric);
};

export default function MoneyInput({
  label,
  value,
  onValueChange,
  error,
  required,
  ...props
}: Props) {
  const { i18n } = useTranslation();

  // Refs so the effect always reads the latest value/callback without re-running
  const isFocusedRef = useRef(false);
  const prevLocaleRef = useRef(i18n.language);
  const valueRef = useRef(value);
  const onValueChangeRef = useRef(onValueChange);
  valueRef.current = value;
  onValueChangeRef.current = onValueChange;

  // When the UI language changes, re-parse the stored formatted value with the
  // OLD locale and re-format it with the NEW locale so the input always shows
  // the correct pattern (e.g. "1.000,50" → "1,000.50" when switching pt-BR → en-US).
  useEffect(() => {
    const prevLocale = prevLocaleRef.current;
    const nextLocale = i18n.language;
    if (prevLocale === nextLocale) return;
    prevLocaleRef.current = nextLocale;

    // Don't interrupt the user while they are actively typing
    if (isFocusedRef.current || !valueRef.current) return;

    const normalized = normalizeMoney(valueRef.current, prevLocale);
    const numeric = Number(normalized);
    if (!Number.isNaN(numeric)) {
      // formatNumber reads i18n.language at call-time → uses the NEW locale
      onValueChangeRef.current(formatNumber(numeric));
    }
  }, [i18n.language]);

  const helperId = useMemo(
    () => `money-input-${label.toLowerCase().replace(/\s+/g, "-")}`,
    [label]
  );

  return (
    <label className="block text-sm">
      <span className="font-medium text-slate-700">
        {label}
        {required ? <span className="text-red-500"> *</span> : null}
      </span>
      <input
        {...props}
        inputMode="decimal"
        value={value}
        onFocus={() => { isFocusedRef.current = true; }}
        onChange={(event) =>
          onValueChange(normalizeMoneyInput(event.target.value, i18n.language))
        }
        onBlur={() => {
          isFocusedRef.current = false;
          onValueChange(formatMoneyValue(value));
        }}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? helperId : undefined}
        className={`mt-1 w-full rounded-md border px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 ${
          error
            ? "border-red-400 focus:ring-red-200"
            : "border-slate-200 focus:ring-slate-200"
        }`}
      />
      {error ? (
        <span id={helperId} className="text-xs text-red-600">
          {error}
        </span>
      ) : null}
    </label>
  );
}
