import { api } from "./api";
import {
  ApiResponse,
  BrandListResponse,
  ColorListResponse,
  ModelListResponse,
} from "./types";

export async function fetchBrands() {
  const response = await api.get<ApiResponse<BrandListResponse>>("/brands");
  return response.data.data.brands;
}

export async function fetchModelsByBrand(brandId: string) {
  const response = await api.get<ApiResponse<ModelListResponse>>(
    `/brands/${brandId}/models`
  );
  return response.data.data.models;
}

export async function fetchColors() {
  const response = await api.get<ApiResponse<ColorListResponse>>("/colors");
  return response.data.data.colors;
}
