package br.com.carreselling.application.service.model;

import java.time.LocalDate;
import java.util.UUID;

public record DistributedVehiclesFilter(LocalDate startDate,
                                        LocalDate endDate,
                                        String brand,
                                        String model,
                                        UUID partnerId) {
}
