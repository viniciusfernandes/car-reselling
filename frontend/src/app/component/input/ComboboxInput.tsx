import { InputHTMLAttributes, useId } from "react";

type Props = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  suggestions: string[];
  error?: string;
};

export default function ComboboxInput({
  label,
  suggestions,
  error,
  required,
  ...props
}: Props) {
  const listId = useId();

  return (
    <label className="block text-sm">
      <span className="font-medium text-slate-700">
        {label}
        {required ? <span className="text-red-500"> *</span> : null}
      </span>
      <input
        {...props}
        list={listId}
        aria-invalid={Boolean(error)}
        className={`mt-1 w-full rounded-md border px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 ${
          error
            ? "border-red-400 focus:ring-red-200"
            : "border-slate-200 focus:ring-slate-200"
        }`}
      />
      <datalist id={listId}>
        {suggestions.map((option) => (
          <option key={option} value={option} />
        ))}
      </datalist>
      {error ? <span className="text-xs text-red-600">{error}</span> : null}
    </label>
  );
}
