package br.com.revendacarros.usecase.veiculo.listar.contract;

import java.util.List;

public record ListaVeiculosResponse(List<ItemListaVeiculo> items, int page, int size, long total) {
}
