import { InputHTMLAttributes, useMemo } from "react";

type Props = Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "value"> & {
  label: string;
  value: string;
  onValueChange: (value: string) => void;
  error?: string;
};

const normalizeMoneyInput = (value: string) => {
  const sanitized = value.replace(",", ".").replace(/[^0-9.]/g, "");
  const [integerPart, decimalPart = ""] = sanitized.split(".");
  const normalizedDecimal = decimalPart.slice(0, 2);
  return normalizedDecimal.length > 0
    ? `${integerPart}.${normalizedDecimal}`
    : integerPart;
};

const formatMoneyValue = (value: string) => {
  if (!value) {
    return "";
  }
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return value;
  }
  return numeric.toFixed(2);
};

export default function MoneyInput({
  label,
  value,
  onValueChange,
  error,
  required,
  ...props
}: Props) {
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
        onChange={(event) => onValueChange(normalizeMoneyInput(event.target.value))}
        onBlur={() => onValueChange(formatMoneyValue(value))}
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
