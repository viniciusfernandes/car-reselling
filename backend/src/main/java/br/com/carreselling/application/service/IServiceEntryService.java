package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.ServiceSummary;
import br.com.carreselling.domain.model.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IServiceEntryService {

    UUID addService(UUID vehicleId,
                    ServiceType serviceType,
                    BigDecimal serviceValue,
                    String description,
                    LocalDate performedAt);

    List<ServiceSummary> listServices(UUID vehicleId);

    BigDecimal totalServices(UUID vehicleId);

    void updateService(UUID vehicleId,
                       UUID serviceId,
                       ServiceType serviceType,
                       BigDecimal serviceValue,
                       String description,
                       LocalDate performedAt);

    void deleteService(UUID vehicleId, UUID serviceId);
}
