package br.com.revendacarros.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Cor {

    private final UUID id;
    private final int empresaId;
    private final String nome;
    private final Instant criadoEm;
    private final Instant atualizadoEm;

    public Cor(UUID id, int empresaId, String nome, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.empresaId = empresaId;
        this.nome = nome;
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

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
