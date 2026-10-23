package br.com.revendacarros.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
public class Pagamento {

    public final UUID id;
    public final int empresaId;
    public TipoPagamento tipoPagamento;
    public String descricao;
    public BigDecimal valor;
    public LocalDate dataPagamento;
    public UUID veiculoId;
    public String veiculoPlaca;
    public String observacoes;
    public Instant criadoEm;
    public Instant atualizadoEm;

    public Pagamento(UUID id,
                   int empresaId,
                   TipoPagamento tipoPagamento,
                   String descricao,
                   BigDecimal valor,
                   LocalDate dataPagamento,
                   UUID veiculoId,
                   String observacoes,
                   Instant criadoEm,
                   Instant atualizadoEm) {
        this.id = id;
        this.empresaId = empresaId;
        this.tipoPagamento = tipoPagamento;
        this.descricao = descricao;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.veiculoId = veiculoId;
        this.observacoes = observacoes;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void update(TipoPagamento tipoPagamento,
                       String descricao,
                       BigDecimal valor,
                       LocalDate dataPagamento,
                       UUID veiculoId,
                       String observacoes) {
        this.tipoPagamento = tipoPagamento;
        this.descricao = descricao;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.veiculoId = veiculoId;
        this.observacoes = observacoes;
    }

    @Override
    public String toString() {
        return "Pagamento{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", tipoPagamento=" + tipoPagamento +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", dataPagamento=" + dataPagamento +
                ", veiculoId=" + veiculoId +
                ", veiculoPlaca='" + veiculoPlaca + '\'' +
                ", observacoes='" + observacoes + '\'' +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                '}';
    }
}
