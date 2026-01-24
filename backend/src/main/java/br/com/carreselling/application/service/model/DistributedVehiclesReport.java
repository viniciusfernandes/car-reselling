package br.com.carreselling.application.service.model;

import java.math.BigDecimal;
import java.util.List;

public record DistributedVehiclesReport(List<ReportPartnerGroup> partners,
                                        int overallVehiclesCount,
                                        BigDecimal overallVehiclesTotalValue) {
}
