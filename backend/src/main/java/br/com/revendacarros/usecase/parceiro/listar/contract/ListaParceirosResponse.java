package br.com.revendacarros.usecase.parceiro.listar.contract;

import java.util.List;

public record ListaParceirosResponse(List<ParceiroItem> parceiros) {
}
