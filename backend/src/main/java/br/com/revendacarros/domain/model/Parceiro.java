package br.com.revendacarros.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Parceiro {

    private final UUID id;
    private final int empresaId;
    private final String nome;
    private final String cidade;
    private final String telefone;
    private final String email;
    private final BigDecimal taxaComissao;
    private final boolean habilitado;
    private final Instant criadoEm;
    private final Instant atualizadoEm;

    public Parceiro(UUID id,
                   int empresaId,
                   String nome,
                   String cidade,
                   String telefone,
                   String email,
                   BigDecimal taxaComissao,
                   boolean habilitado,
                   Instant criadoEm,
                   Instant atualizadoEm) {
        this.id = id;
        this.empresaId = empresaId;
        this.nome = nome;
        this.cidade = cidade;
        this.telefone = telefone;
        this.email = email;
        this.taxaComissao = taxaComissao;
        this.habilitado = habilitado;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public int getEmpresaId() {
        return empresaId;
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

    public boolean isHabilitado() {
        return habilitado;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
