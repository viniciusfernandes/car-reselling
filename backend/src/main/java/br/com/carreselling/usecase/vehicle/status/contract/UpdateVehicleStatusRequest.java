package br.com.carreselling.usecase.vehicle.status.contract;

import br.com.carreselling.domain.model.VehicleStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateVehicleStatusRequest(@NotNull VehicleStatus status, UUID assignedPartnerId) {
}
