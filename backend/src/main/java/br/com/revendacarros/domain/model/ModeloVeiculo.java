package br.com.revendacarros.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ModeloVeiculo {

    private final UUID id;
    private final int empresaId;
    private final UUID marcaId;
    private final String nome;
    private final Instant criadoEm;
    private final Instant atualizadoEm;

    public ModeloVeiculo(UUID id, int empresaId, UUID marcaId, String nome, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.empresaId = empresaId;
        this.marcaId = marcaId;
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

    public UUID getMarcaId() {
        return marcaId;
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
