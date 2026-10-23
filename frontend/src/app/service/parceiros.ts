import { api } from "./api";
import type {
  ApiResponse,
  DetalheParceiro,
  HistoricoParceiroResponse,
  ListaParceirosResponse,
} from "./types";

export async function fetchParceiros(): Promise<ListaParceirosResponse> {
  const response = await api.get<ApiResponse<ListaParceirosResponse>>("/parceiros");
  return response.data.data;
}

export async function fetchParceiro(id: string): Promise<DetalheParceiro> {
  const response = await api.get<ApiResponse<DetalheParceiro>>(`/parceiros/${id}`);
  return response.data.data;
}

export type AtualizarParceiroPayload = {
  nome: string;
  cidade?: string;
  telefone?: string;
  email?: string;
  taxaComissao?: number | null;
};

export async function updateParceiro(
  id: string,
  payload: AtualizarParceiroPayload
): Promise<void> {
  await api.put(`/parceiros/${id}`, payload);
}

export async function fetchHistoricoParceiro(
  id: string
): Promise<HistoricoParceiroResponse> {
  const response = await api.get<ApiResponse<HistoricoParceiroResponse>>(
    `/parceiros/${id}/historico`
  );
  return response.data.data;
}

export type CriarParceiroPayload = {
  nome: string;
  cidade?: string;
  telefone?: string;
  email?: string;
  taxaComissao?: number | null;
};

export type CriarParceiroResponse = {
  parceiroId: string;
};

export async function criarParceiro(
  payload: CriarParceiroPayload
): Promise<CriarParceiroResponse> {
  const response = await api.post<ApiResponse<CriarParceiroResponse>>(
    "/parceiros",
    payload
  );
  return response.data.data;
}

export async function desabilitarParceiro(id: string): Promise<void> {
  await api.delete(`/parceiros/${id}`);
}
