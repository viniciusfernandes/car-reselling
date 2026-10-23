import axios from "axios";
import i18n from "../i18n";
import type {
  ApiResponse,
  SaldoCaixaData,
  PainelFinanceiroData,
  TipoPagamento,
  ListaPagamentosResponse,
  PagamentoItem,
  ListaDocumentosPagamentoResponse,
  CriarPagamentoRequest,
  AtualizarPagamentoRequest,
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

export const financeiroApi = {
  getPainelFinanceiro(dataInicio?: string, dataFim?: string) {
    return api.get<ApiResponse<PainelFinanceiroData>>("/relatorios/painel-financeiro", {
      params: { dataInicio, dataFim },
    });
  },
};

export const saldoCaixaApi = {
  get() {
    return api.get<ApiResponse<SaldoCaixaData>>("/financeiro/saldo-caixa");
  },
  update(valor: number) {
    return api.put<ApiResponse<SaldoCaixaData>>("/financeiro/saldo-caixa", { valor });
  },
};

export const pagamentosApi = {
  list(params?: { tipoPagamento?: TipoPagamento; mesPagamento?: string; placa?: string }) {
    return api.get<ApiResponse<ListaPagamentosResponse>>("/pagamentos", { params });
  },

  get(id: string) {
    return api.get<ApiResponse<PagamentoItem>>(`/pagamentos/${id}`);
  },

  create(data: CriarPagamentoRequest) {
    return api.post<ApiResponse<{ id: string }>>("/pagamentos", data);
  },

  update(id: string, data: AtualizarPagamentoRequest) {
    return api.put<ApiResponse<void>>(`/pagamentos/${id}`, data);
  },

  delete(id: string) {
    return api.delete(`/pagamentos/${id}`);
  },

  listarDocumentos(pagamentoId: string) {
    return api.get<ApiResponse<ListaDocumentosPagamentoResponse>>(`/pagamentos/${pagamentoId}/documentos`);
  },

  uploadDocumento(pagamentoId: string, file: File) {
    const formData = new FormData();
    formData.append("file", file);
    return api.post<ApiResponse<{ documentoId: string }>>(`/pagamentos/${pagamentoId}/documentos`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  downloadDocumento(pagamentoId: string, documentoId: string) {
    return api.get(`/pagamentos/${pagamentoId}/documentos/${documentoId}/download`, {
      responseType: "blob",
    });
  },

  deleteDocumento(pagamentoId: string, documentoId: string) {
    return api.delete(`/pagamentos/${pagamentoId}/documentos/${documentoId}`);
  },

  listarDescricoes(tipoPagamento?: TipoPagamento) {
    return api.get<ApiResponse<string[]>>("/pagamentos/descricoes", {
      params: tipoPagamento ? { tipoPagamento } : undefined,
    });
  },
};
