package br.com.revendacarros.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ServicoNoVeiculo {

    private final UUID id;
    private final int empresaId;
    private final UUID veiculoId;
    private TipoServico tipoServico;
    private String descricao;
    private BigDecimal valorServico;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Instant criadoEm;
    private Instant atualizadoEm;

    public ServicoNoVeiculo(UUID id,
                            int empresaId,
                            UUID veiculoId,
                            LocalDate dataInicio,
                            LocalDate dataFim) {
        this.id = id;
        this.empresaId = empresaId;
        this.veiculoId = veiculoId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public ServicoNoVeiculo(UUID id,
                            int empresaId,
                            UUID veiculoId,
                            TipoServico tipoServico,
                            String descricao,
                            BigDecimal valorServico,
                            LocalDate dataInicio,
                            LocalDate dataFim,
                            Instant criadoEm,
                            Instant atualizadoEm) {
        this.id = id;
        this.empresaId = empresaId;
        this.veiculoId = veiculoId;
        this.tipoServico = tipoServico;
        this.descricao = descricao;
        this.valorServico = valorServico;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public TipoServico getTipoServico() {
        return tipoServico;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValorServico() {
        return valorServico;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void update(TipoServico tipoServico, String descricao, BigDecimal valorServico,
                       LocalDate dataInicio, LocalDate dataFim) {
        this.tipoServico = tipoServico;
        this.descricao = descricao;
        this.valorServico = valorServico;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
