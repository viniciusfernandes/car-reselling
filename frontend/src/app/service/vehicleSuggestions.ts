import { api } from "./api";
import { ApiResponse, VehicleDetail, VehicleListResponse } from "./types";

export type VehicleSuggestions = {
  colors: string[];
  brands: string[];
  models: string[];
};

const uniqueSorted = (values: string[]) =>
  Array.from(
    new Set(values.map((value) => value.trim()).filter(Boolean))
  ).sort((a, b) => a.localeCompare(b));

export async function fetchVehicleSuggestions(
  limit = 200
): Promise<VehicleSuggestions> {
  const listResponse = await api.get<ApiResponse<VehicleListResponse>>(
    "/vehicles",
    {
      params: {
        page: 0,
        size: limit,
      },
    }
  );
  const items = listResponse.data.data.items;

  const brands = uniqueSorted(items.map((item) => item.brand));
  const models = uniqueSorted(items.map((item) => item.model));

  const colors = new Set<string>();
  await Promise.all(
    items.map(async (item) => {
      try {
        const detailResponse = await api.get<ApiResponse<VehicleDetail>>(
          `/vehicles/${item.id}`
        );
        if (detailResponse.data.data.color) {
          colors.add(detailResponse.data.data.color);
        }
      } catch {
        // Best effort: skip items that fail to load.
      }
    })
  );

  return {
    colors: uniqueSorted([...colors]),
    brands,
    models,
  };
}
