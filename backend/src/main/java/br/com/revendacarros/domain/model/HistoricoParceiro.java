package br.com.revendacarros.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class HistoricoParceiro {

    private final UUID id;
    private final int empresaId;
    private final UUID parceiroId;
    private final String nome;
    private final String cidade;
    private final String telefone;
    private final String email;
    private final BigDecimal taxaComissao;
    private final Instant alteradoEm;
    private final String alteradoPor;

    public HistoricoParceiro(UUID id,
                          int empresaId,
                          UUID parceiroId,
                          String nome,
                          String cidade,
                          String telefone,
                          String email,
                          BigDecimal taxaComissao,
                          Instant alteradoEm,
                          String alteradoPor) {
        this.id = id;
        this.empresaId = empresaId;
        this.parceiroId = parceiroId;
        this.nome = nome;
        this.cidade = cidade;
        this.telefone = telefone;
        this.email = email;
        this.taxaComissao = taxaComissao;
        this.alteradoEm = alteradoEm;
        this.alteradoPor = alteradoPor;
    }

    public UUID getId() {
        return id;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public UUID getParceiroId() {
        return parceiroId;
    }

    public String getNome() {
        return nome;
    }

    public String getCidade() {
        return cidade;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getTaxaComissao() {
        return taxaComissao;
    }

    public Instant getAlteradoEm() {
        return alteradoEm;
    }

    public String getAlteradoPor() {
        return alteradoPor;
    }
}
