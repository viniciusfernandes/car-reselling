import axios from "axios";
import i18n from "../i18n";

const AUTH_TOKEN_KEY = "auth_token";
const AUTH_TOKEN_EVENT = "auth-token-changed";

export const api = axios.create({
  baseURL: "/api/v1",
});

export type ApiError = {
  errors?: string[];
  error?: string;
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(AUTH_TOKEN_KEY);
  if (token) {
    config.headers = {
      ...config.headers,
      Authorization: `Bearer ${token}`,
    };
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem(AUTH_TOKEN_KEY);
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export const authTokenStorage = {
  get() {
    return localStorage.getItem(AUTH_TOKEN_KEY);
  },
  set(token: string) {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
    window.dispatchEvent(new Event(AUTH_TOKEN_EVENT));
  },
  clear() {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    window.dispatchEvent(new Event(AUTH_TOKEN_EVENT));
  },
};

export const authTokenEvents = {
  changeEvent: AUTH_TOKEN_EVENT,
};

export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiError>(error)) {
    const errorMessage = error.response?.data?.error;
    if (errorMessage) {
      return errorMessage;
    }
    const errors = error.response?.data?.errors;
    if (errors && errors.length > 0) {
      return errors[0];
    }
  }
  return i18n.t("errors.unexpected");
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
