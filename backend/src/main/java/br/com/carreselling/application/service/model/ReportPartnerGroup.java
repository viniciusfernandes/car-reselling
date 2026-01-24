package br.com.carreselling.application.service.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReportPartnerGroup(UUID partnerId,
                                 String partnerName,
                                 List<ReportVehicleItem> vehicles,
                                 BigDecimal partnerVehiclesTotalValue,
                                 int partnerVehiclesCount) {
}
