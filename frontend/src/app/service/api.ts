import axios from "axios";

export const api = axios.create({
  baseURL: "/api/v1",
});

export type ApiError = {
  errors?: string[];
};

export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiError>(error)) {
    const errors = error.response?.data?.errors;
    if (errors && errors.length > 0) {
      return errors[0];
    }
  }
  return "Unexpected error";
}

export function extractFieldErrors(errors?: string[]) {
  if (!errors) {
    return {};
  }
  return errors.reduce<Record<string, string>>((acc, message) => {
    const [field, rest] = message.split(":").map((part) => part.trim());
    if (field && rest) {
      acc[field] = rest;
    }
    return acc;
  }, {});
}
