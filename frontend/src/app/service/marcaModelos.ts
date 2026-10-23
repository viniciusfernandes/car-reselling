import { api } from "./api";
import {
  ApiResponse,
  ListaMarcasResponse,
  ListaCoresResponse,
  ModeloListResponse,
} from "./types";

export async function fetchMarcas() {
  const response = await api.get<ApiResponse<ListaMarcasResponse>>("/marcas");
  return response.data.data.marcas;
}

export async function fetchModelosByMarca(marcaId: string) {
  const response = await api.get<ApiResponse<ModeloListResponse>>(
    `/marcas/${marcaId}/modelos`
  );
  return response.data.data.modelos;
}

export async function fetchCores() {
  const response = await api.get<ApiResponse<ListaCoresResponse>>("/cores");
  return response.data.data.cores;
}
