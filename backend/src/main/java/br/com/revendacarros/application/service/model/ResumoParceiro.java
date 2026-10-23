package br.com.revendacarros.application.service.model;

import java.util.UUID;

import java.math.BigDecimal;

public record ResumoParceiro(UUID id, String nome, String cidade, String telefone, String email, BigDecimal taxaComissao) {
}
