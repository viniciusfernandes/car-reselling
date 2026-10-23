package br.com.revendacarros.domain.model;

import br.com.revendacarros.common.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

public class HistoricoSaldoCaixa {

    public UUID id;
    public int empresaId;
    public double valor;
    public LocalDateTime alteradoEm;
    public String alteradoPor;

    public HistoricoSaldoCaixa(int empresaId, double valor, String alteradoPor) {
        this.id = UuidGenerator.generate();
        this.empresaId = empresaId;
        this.valor = valor;
        this.alteradoEm = LocalDateTime.now();
        this.alteradoPor = alteradoPor;
    }

    public int getEmpresaId() {
        return empresaId;
    }
}
