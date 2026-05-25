import { SelectHTMLAttributes } from "react";

type Option = { value: string; label: string };

type Props = SelectHTMLAttributes<HTMLSelectElement> & {
  label: string;
  error?: string;
  options: Option[];
};

export default function SelectInput({
  label,
  error,
  options,
  required,
  disabled,
  ...props
}: Props) {
  return (
    <label className="block text-sm">
      <span className={`font-medium ${disabled ? "text-slate-400" : "text-slate-700"}`}>
        {label}
        {required ? <span className="text-red-500"> *</span> : null}
      </span>
      <select
        {...props}
        disabled={disabled}
        aria-invalid={Boolean(error)}
        className={`mt-1 w-full rounded-md border px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 ${
          disabled
            ? "border-slate-200 bg-slate-100 text-slate-400 cursor-not-allowed"
            : error
              ? "border-red-400 focus:ring-red-200"
              : "border-slate-200 focus:ring-slate-200"
        }`}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error ? <span className="text-xs text-red-600">{error}</span> : null}
    </label>
  );
}
