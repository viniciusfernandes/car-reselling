import { api } from "./api";
import type {
  ApiResponse,
  PartnerDetail,
  PartnerHistoryResponse,
  PartnerListResponse,
} from "./types";

export async function fetchPartners(): Promise<PartnerListResponse> {
  const response = await api.get<ApiResponse<PartnerListResponse>>("/partners");
  return response.data.data;
}

export async function fetchPartner(id: string): Promise<PartnerDetail> {
  const response = await api.get<ApiResponse<PartnerDetail>>(`/partners/${id}`);
  return response.data.data;
}

export type UpdatePartnerPayload = {
  name: string;
  city?: string;
  phone?: string;
  email?: string;
  commissionRate?: number | null;
};

export async function updatePartner(
  id: string,
  payload: UpdatePartnerPayload
): Promise<void> {
  await api.put(`/partners/${id}`, payload);
}

export async function fetchPartnerHistory(
  id: string
): Promise<PartnerHistoryResponse> {
  const response = await api.get<ApiResponse<PartnerHistoryResponse>>(
    `/partners/${id}/history`
  );
  return response.data.data;
}
