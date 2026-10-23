package br.com.revendacarros.usecase.marca.listar.contract;

import java.util.List;

public record ListaMarcasResponse(List<MarcaItem> marcas) {
}
