package br.com.carreselling.usecase.vehicle.distribution.contract;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignPartnerRequest(@NotNull UUID partnerId) {
}
