import i18n from "../i18n";

const getLocale = () => i18n.language || "pt-BR";

export const formatMoney = (value: number) =>
  new Intl.NumberFormat(getLocale(), {
    style: "currency",
    currency: "BRL",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);

export const formatNumber = (value: number) =>
  new Intl.NumberFormat(getLocale(), {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);

export const formatDate = (value?: string | null) => {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "-";
  }
  return date.toLocaleDateString(getLocale());
};

export const normalizeMoney = (value: string, locale = getLocale()) => {
  const sanitized = value.replace(/\s/g, "").replace(/[^0-9.,-]/g, "");
  if (locale.startsWith("pt")) {
    return sanitized.replace(/\./g, "").replace(",", ".");
  }
  return sanitized.replace(/,/g, "");
};

export const parseMoney = (value: string) => {
  if (!value) {
    return Number.NaN;
  }
  const normalized = normalizeMoney(value);
  if (!normalized) {
    return Number.NaN;
  }
  const numeric = Number(normalized);
  return Number.isNaN(numeric) ? Number.NaN : numeric;
};
