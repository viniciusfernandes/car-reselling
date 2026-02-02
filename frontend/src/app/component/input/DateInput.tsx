import { InputHTMLAttributes } from "react";
import { useTranslation } from "react-i18next";

type Props = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

export default function DateInput({ label, error, required, ...props }: Props) {
  const { i18n } = useTranslation();
  return (
    <label className="block text-sm">
      <span className="font-medium text-slate-700">
        {label}
        {required ? <span className="text-red-500"> *</span> : null}
      </span>
      <input
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
