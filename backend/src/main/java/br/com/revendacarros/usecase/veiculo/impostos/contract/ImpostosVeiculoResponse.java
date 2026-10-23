package br.com.revendacarros.usecase.veiculo.impostos.contract;

import java.math.BigDecimal;

public record ImpostosVeiculoResponse(BigDecimal icms,
                                   BigDecimal pis,
                                   BigDecimal cofins,
                                   BigDecimal csll,
                                   BigDecimal irpj,
                                   BigDecimal totalImpostos) {
}
