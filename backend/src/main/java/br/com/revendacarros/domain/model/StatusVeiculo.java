package br.com.revendacarros.domain.model;

public enum StatusVeiculo {
    NO_PATIO,
    PRONTO_PARA_DISTRIBUICAO,
    DISTRIBUIDO,
    VENDIDO;

    public boolean jaDistribuido() {
        return this == DISTRIBUIDO || this == VENDIDO;
    }

    public boolean isTransitionAllowed(StatusVeiculo target) {
        return this != VENDIDO || target == VENDIDO;
    }
}
