package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.ServiceSummary;
import br.com.carreselling.common.UuidGenerator;
import br.com.carreselling.domain.exception.NotFoundException;
import br.com.carreselling.domain.model.ServiceOnVehicle;
import br.com.carreselling.domain.model.ServiceType;
import br.com.carreselling.domain.model.Vehicle;
import br.com.carreselling.domain.repository.ServiceRepository;
import br.com.carreselling.domain.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

@Service
public class ServiceOnVehicleService implements IServiceEntryService {

    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;

    public ServiceOnVehicleService(VehicleRepository vehicleRepository, ServiceRepository serviceRepository) {
        this.vehicleRepository = vehicleRepository;
        this.serviceRepository = serviceRepository;
    }

    @Override
    public UUID addService(UUID vehicleId,
                           ServiceType serviceType,
                           BigDecimal serviceValue,
                           String description,
                           LocalDate startDate,
                           LocalDate endDate) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        vehicle.ensureServicesEditable();
        if (serviceValue == null || serviceValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("serviceValue: cannot be negative.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("startDate: required.");
        }
        ServiceOnVehicle entry = new ServiceOnVehicle(
                UuidGenerator.generate(),
                vehicleId,
                serviceType,
                description,
                serviceValue,
                startDate,
                endDate,
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
                        service.getStartDate(),
                        service.getEndDate()
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
                              LocalDate startDate,
                              LocalDate endDate) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        vehicle.ensureServicesEditable();
        if (startDate == null) {
            throw new IllegalArgumentException("startDate: required.");
        }
        ServiceOnVehicle service = serviceRepository.findServiceById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));
        if (!service.getVehicleId().equals(vehicleId)) {
            throw new NotFoundException("Service not found for vehicle");
        }
        service.update(serviceType, description, serviceValue, startDate, endDate);
        service.setUpdatedAt(Instant.now());
        serviceRepository.updateService(service);
    }

    @Override
    public void deleteService(UUID vehicleId, UUID serviceId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        vehicle.ensureServicesEditable();
        ServiceOnVehicle service = serviceRepository.findServiceById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));
        if (!service.getVehicleId().equals(vehicleId)) {
            throw new NotFoundException("Service not found for vehicle");
        }
        serviceRepository.deleteService(serviceId);
    }

    @Override
    public long calculateTotalServiceDays(List<ServiceOnVehicle> services) {
        if (services == null || services.isEmpty()) {
            return 0L;
        }
        List<ServiceOnVehicle> orderedServices = services.stream()
                .sorted((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                .toList();

        ServiceOnVehicle first = orderedServices.getFirst();
        LocalDate now = LocalDate.now();
        if (!first.getStartDate().isBefore(now)) {
            return 0;
        }

        Stack<LocalDate> dates = new Stack<>();
        LocalDate start = first.getStartDate();
        LocalDate end = first.getEndDate() == null || first.getEndDate().isAfter(now) ? now : first.getEndDate();

        if (services.size() == 1) {
            return daysBetween(start, end);
        }

        dates.add(start);
        dates.add(end);
        for (int j = 1; j < orderedServices.size(); j++) {
            LocalDate last = dates.pop();
            if (!last.isBefore(now)) {
                dates.add(now);
                break;
            }
            ServiceOnVehicle next = orderedServices.get(j);
            if (last.isBefore(next.getStartDate())) {
                dates.add(last);
                dates.add(next.getStartDate());
                if (next.getEndDate() != null) {
                    dates.add(next.getEndDate());
                }
            } else if (next.getEndDate() != null && next.getEndDate() != null && last.isBefore(next.getEndDate())) {
                dates.add(next.getEndDate());
            } else if (next.getEndDate() != null && !last.isBefore(next.getEndDate())) {
                dates.add(last);
            } else if (next.getEndDate() == null) {
                dates.add(now);
            }
            j++;
        }
        return totalDays(dates);
    }

    @Override
    public double calculateTotalServiceDays(UUID vehicleId) {
        List<ServiceOnVehicle> services = serviceRepository.findServiceByVehicleId(vehicleId);
        return calculateTotalServiceDays(services);
    }

    private long totalDays(Stack<LocalDate> dates) {
        int j = 0;
        long totalDays = 0;
        while (j < dates.size()) {
            totalDays += daysBetween(dates.get(j), dates.get(j + 1));
            j = j + 2;
        }
        return totalDays;
    }

    private long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

}
