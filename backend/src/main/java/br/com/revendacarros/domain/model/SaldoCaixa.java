package br.com.revendacarros.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class SaldoCaixa {

    public int empresaId;
    public BigDecimal valor;
    public Instant atualizadoEm;

    public SaldoCaixa(int empresaId, BigDecimal valor, Instant atualizadoEm) {
        this.empresaId = empresaId;
        this.valor = valor;
        this.atualizadoEm = atualizadoEm;
    }

    public int getEmpresaId() {
        return empresaId;
    }
}
