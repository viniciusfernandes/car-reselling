import { api } from "./api";
import { ApiResponse, DetalheVeiculo, ListaVeiculosResponse } from "./types";

export type VeiculoSuggestions = {
  cores: string[];
  marcas: string[];
  modelos: string[];
};

const uniqueSorted = (values: string[]) =>
  Array.from(
    new Set(values.map((value) => value.trim()).filter(Boolean))
  ).sort((a, b) => a.localeCompare(b));

export async function fetchVeiculoSuggestions(
  limit = 20
): Promise<VeiculoSuggestions> {
  const listResponse = await api.get<ApiResponse<ListaVeiculosResponse>>(
    "/veiculos",
    {
      params: {
        page: 0,
        size: limit,
      },
    }
  );
  const items = listResponse.data.data.items;

  const marcas = uniqueSorted(items.map((item) => item.marca));
  const modelos = uniqueSorted(items.map((item) => item.modelo));

  const cores = new Set<string>();
  await Promise.all(
    items.map(async (item) => {
      try {
        const detailResponse = await api.get<ApiResponse<DetalheVeiculo>>(
          `/veiculos/${item.id}`
        );
        if (detailResponse.data.data.cor) {
          cores.add(detailResponse.data.data.cor);
        }
      } catch {
        // Best effort: skip items that fail to load.
      }
    })
  );

  return {
    cores: uniqueSorted([...cores]),
    marcas,
    modelos,
  };
}
