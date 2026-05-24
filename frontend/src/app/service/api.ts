import axios from "axios";
import i18n from "../i18n";
import type {
  ApiResponse,
  PaymentType,
  PaymentListResponse,
  PaymentItem,
  PaymentDocumentListResponse,
  CreatePaymentRequest,
  UpdatePaymentRequest,
} from "./types";

const AUTH_TOKEN_KEY = "auth_token";
const AUTH_TOKEN_EVENT = "auth-token-changed";

const apiBaseUrl =
  import.meta.env.VITE_API_BASE_URL != null && import.meta.env.VITE_API_BASE_URL !== ""
    ? `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, "")}/api/v1`
    : "/api/v1";

export const api = axios.create({
  baseURL: apiBaseUrl,
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

export const paymentsApi = {
  list(params?: { paymentType?: PaymentType; referenceMonth?: string; licensePlate?: string }) {
    return api.get<ApiResponse<PaymentListResponse>>("/payments", { params });
  },

  get(id: string) {
    return api.get<ApiResponse<PaymentItem>>(`/payments/${id}`);
  },

  create(data: CreatePaymentRequest) {
    return api.post<ApiResponse<{ id: string }>>("/payments", data);
  },

  update(id: string, data: UpdatePaymentRequest) {
    return api.put<ApiResponse<void>>(`/payments/${id}`, data);
  },

  delete(id: string) {
    return api.delete(`/payments/${id}`);
  },

  listDocuments(paymentId: string) {
    return api.get<ApiResponse<PaymentDocumentListResponse>>(`/payments/${paymentId}/documents`);
  },

  uploadDocument(paymentId: string, file: File) {
    const formData = new FormData();
    formData.append("file", file);
    return api.post<ApiResponse<{ documentId: string }>>(`/payments/${paymentId}/documents`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  downloadDocument(paymentId: string, documentId: string) {
    return api.get(`/payments/${paymentId}/documents/${documentId}/download`, {
      responseType: "blob",
    });
  },

  deleteDocument(paymentId: string, documentId: string) {
    return api.delete(`/payments/${paymentId}/documents/${documentId}`);
  },

  listDescriptions(paymentType?: PaymentType) {
    return api.get<ApiResponse<string[]>>("/payments/descriptions", {
      params: paymentType ? { paymentType } : undefined,
    });
  },
};
