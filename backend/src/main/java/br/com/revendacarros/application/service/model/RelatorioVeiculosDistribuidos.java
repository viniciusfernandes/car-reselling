package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioVeiculosDistribuidos(List<GrupoParceiroRelatorio> parceiros,
                                        int quantidadeGeralVeiculos,
                                        BigDecimal valorTotalGeralVeiculos) {
}
