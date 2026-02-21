import { ChangeEvent, InputHTMLAttributes } from "react";
import { useTranslation } from "react-i18next";
import DatePicker from "react-datepicker";
import { enUS, ptBR } from "date-fns/locale";
import "react-datepicker/dist/react-datepicker.css";

type Props = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

// Locale-aware date format hints shown as placeholder text.
// For type="date" the browser renders its own picker so the placeholder is
// invisible, but for type="text" date fields it provides a useful format hint.
const DATE_PLACEHOLDER: Record<string, string> = {
  "pt-BR": "dd/mm/yyyy",
  "en-US": "mm/dd/yyyy",
};

const parseDateValue = (value?: string) => {
  if (!value) return null;
  const [year, month, day] = value.split("-").map(Number);
  if (!year || !month || !day) return null;
  const parsed = new Date(year, month - 1, day);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
};

const toDateValue = (value: Date | null) => {
  if (!value) return "";
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, "0");
  const day = String(value.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

export default function DateInput({ label, error, required, ...props }: Props) {
  const { i18n } = useTranslation();

  const localePlaceholder =
    DATE_PLACEHOLDER[i18n.language] ?? DATE_PLACEHOLDER["en-US"];
  const locale = i18n.language === "pt-BR" ? ptBR : enUS;

  if (props.type === "date") {
    const {
      value,
      onChange,
      onBlur,
      min,
      max,
      disabled,
      name,
      id,
      required: inputRequired,
    } = props;
    const selectedDate = parseDateValue(typeof value === "string" ? value : undefined);

    return (
      <label className="block text-sm">
        <span className="font-medium text-slate-700">
          {label}
          {required ? <span className="text-red-500"> *</span> : null}
        </span>
        <DatePicker
          key={`${i18n.language}-date-picker`}
          selected={selectedDate}
          onChange={(date) => {
            const nextValue = toDateValue(date);
            if (onChange) {
              onChange({ target: { value: nextValue } } as ChangeEvent<HTMLInputElement>);
            }
          }}
          onBlur={onBlur}
          locale={locale}
          dateFormat={i18n.language === "pt-BR" ? "dd/MM/yyyy" : "MM/dd/yyyy"}
          placeholderText={localePlaceholder}
          minDate={parseDateValue(typeof min === "string" ? min : undefined)}
          maxDate={parseDateValue(typeof max === "string" ? max : undefined)}
          disabled={disabled}
          name={name}
          id={id}
          required={inputRequired}
          className={`mt-1 w-full rounded-md border px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 ${
            error
              ? "border-red-400 focus:ring-red-200"
              : "border-slate-200 focus:ring-slate-200"
          }`}
        />
        {error ? <span className="text-xs text-red-600">{error}</span> : null}
      </label>
    );
  }

  return (
    <label className="block text-sm">
      <span className="font-medium text-slate-700">
        {label}
        {required ? <span className="text-red-500"> *</span> : null}
      </span>
      <input
        key={`${i18n.language}-${props.type ?? "text"}`}
        placeholder={localePlaceholder}
        {...props}
        lang={i18n.language}
        aria-invalid={Boolean(error)}
        className={`mt-1 w-full rounded-md border px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 ${
          error
            ? "border-red-400 focus:ring-red-200"
            : "border-slate-200 focus:ring-slate-200"
        }`}
      />
      {error ? <span className="text-xs text-red-600">{error}</span> : null}
    </label>
  );
}
