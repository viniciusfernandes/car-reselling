package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.ServiceSummary;
import br.com.carreselling.domain.exception.NotFoundException;
import br.com.carreselling.domain.model.ServiceEntry;
import br.com.carreselling.domain.model.ServiceType;
import br.com.carreselling.domain.model.Vehicle;
import br.com.carreselling.domain.repository.ServiceRepository;
import br.com.carreselling.domain.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ServiceEntryService implements IServiceEntryService {

    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;

    public ServiceEntryService(VehicleRepository vehicleRepository, ServiceRepository serviceRepository) {
        this.vehicleRepository = vehicleRepository;
        this.serviceRepository = serviceRepository;
    }

    @Override
    public UUID addService(UUID vehicleId,
                           ServiceType serviceType,
                           BigDecimal serviceValue,
                           String description,
                           LocalDate performedAt) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        vehicle.ensureServicesEditable();
        if (serviceValue == null || serviceValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("serviceValue: cannot be negative.");
        }
        ServiceEntry entry = new ServiceEntry(
            UUID.randomUUID(),
            vehicleId,
            serviceType,
            description,
            serviceValue,
            performedAt == null ? LocalDate.now() : performedAt,
            Instant.now(),
            Instant.now()
        );
        serviceRepository.saveService(entry);
        return entry.getId();
    }

    @Override
    public List<ServiceSummary> listServices(UUID vehicleId) {
        vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        return serviceRepository.findServiceByVehicleId(vehicleId)
            .stream()
            .map(service -> new ServiceSummary(
                service.getId(),
                service.getVehicleId(),
                service.getServiceType(),
                service.getDescription(),
                service.getServiceValue(),
                service.getPerformedAt()
            ))
            .toList();
    }

    @Override
    public BigDecimal totalServices(UUID vehicleId) {
        return serviceRepository.findServiceTotalByVehicleId(vehicleId);
    }

    @Override
    public void updateService(UUID vehicleId,
                              UUID serviceId,
                              ServiceType serviceType,
                              BigDecimal serviceValue,
                              String description,
                              LocalDate performedAt) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        vehicle.ensureServicesEditable();
        ServiceEntry service = serviceRepository.findServiceById(serviceId)
            .orElseThrow(() -> new NotFoundException("Service not found"));
        if (!service.getVehicleId().equals(vehicleId)) {
            throw new NotFoundException("Service not found for vehicle");
        }
        service.update(serviceType, description, serviceValue, performedAt);
        service.setUpdatedAt(Instant.now());
        serviceRepository.updateService(service);
    }

    @Override
    public void deleteService(UUID vehicleId, UUID serviceId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        vehicle.ensureServicesEditable();
        ServiceEntry service = serviceRepository.findServiceById(serviceId)
            .orElseThrow(() -> new NotFoundException("Service not found"));
        if (!service.getVehicleId().equals(vehicleId)) {
            throw new NotFoundException("Service not found for vehicle");
        }
        serviceRepository.deleteService(serviceId);
    }
}
