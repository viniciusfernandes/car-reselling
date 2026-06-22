package br.com.carreselling.domain.model;

import java.time.Instant;
import java.util.UUID;

public record VehicleOnServiceHistory(UUID id, int companyId, UUID vehicleId, boolean onService, Instant changedAt) {
}
