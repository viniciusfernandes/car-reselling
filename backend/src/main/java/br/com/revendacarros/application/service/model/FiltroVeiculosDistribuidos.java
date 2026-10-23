package br.com.revendacarros.application.service.model;

import java.time.LocalDate;
import java.util.UUID;

public record FiltroVeiculosDistribuidos(LocalDate dataInicio,
                                        LocalDate dataFim,
                                        String marca,
                                        String modelo,
                                        UUID parceiroId) {
}
