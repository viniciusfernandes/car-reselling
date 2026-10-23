package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;

public record PontoMensalFinanceiro(int ano,
                                    int mes,
                                    BigDecimal lucroVendas,
                                    BigDecimal despesas,
                                    BigDecimal net) {
}
