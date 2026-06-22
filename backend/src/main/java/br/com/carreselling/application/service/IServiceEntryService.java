package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.ServiceSummary;
import br.com.carreselling.domain.model.ServiceOnVehicle;
import br.com.carreselling.domain.model.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IServiceEntryService {

    UUID addService(int companyId,
                    UUID vehicleId,
                    ServiceType serviceType,
                    BigDecimal serviceValue,
                    String description,
                    LocalDate startDate,
                    LocalDate endDate);

    List<ServiceSummary> listServices(int companyId, UUID vehicleId);

    BigDecimal totalServices(int companyId, UUID vehicleId);

    void updateService(int companyId,
                       UUID vehicleId,
                       UUID serviceId,
                       ServiceType serviceType,
                       BigDecimal serviceValue,
                       String description,
                       LocalDate startDate,
                       LocalDate endDate);

    void deleteService(int companyId, UUID vehicleId, UUID serviceId);

    long calculateTotalServiceDays(int companyId, List<ServiceOnVehicle> services);

    double calculateTotalServiceDays(int companyId, UUID vehicleId);
}
