package br.com.carreselling.application.service;

import br.com.carreselling.domain.model.ServiceOnVehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class VehicleDaysOnServiceCalculationTest {
    @InjectMocks
    ServiceOnVehicleService serviceOnVehicleService;
    private final UUID serviceId = UUID.randomUUID();
    private final UUID vehicleId = UUID.randomUUID();
    private static final int COMPANY_ID = 1;

    @Test
    void testDaysFromASingleIntervalOneDayAheadToday() {
        List<ServiceOnVehicle> services = buildServicesOneDayAheadToday();
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,services)).isEqualByComparingTo(1L);
    }

    @Test
    void testDaysFromTwoDisjoinedIntervalsAndDaysAheadToday() {
        List<ServiceOnVehicle> services = buildServicesDisjoinedWithAllAheadDates();
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,services)).isEqualByComparingTo(2L);
    }

    @Test
    void testDaysFromTwoJoinedIntervalsAndDaysAhead() {
        List<ServiceOnVehicle> services = buildServicesOnVehicleJoinedAheadDates();
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,services)).isEqualByComparingTo(2L);
    }

    @Test
    void testDaysFromFinishedService() {
        List<ServiceOnVehicle> services = buildWithFinishedServices();
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,services)).isEqualByComparingTo(2L);
    }

    @Test
    void testDaysFromFinishedServiceWithJoinedDates() {
        List<ServiceOnVehicle> services = buildWithFinishedServicesWithJoinedDates();
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,services)).isEqualByComparingTo(15L);
    }

    @Test
    void testDaysFromServiceWithNoEndDate() {
        List<ServiceOnVehicle> services = buildServiceWithNoEndDate();
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,services)).isEqualByComparingTo(10L);
    }

    @Test
    void testDaysFromTwoServiceWithNoEndDate() {
        List<ServiceOnVehicle> services = buildTwoServiceWithNoEndDate();
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,services)).isEqualByComparingTo(25L);
    }

    @Test
    void testDaysFromTwoServiceWithOnlyOneWithNoEndDate() {
        List<ServiceOnVehicle> services = buildTwoServiceWithOnlyOneWithNoEndDate();
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,services)).isEqualByComparingTo(30L);
    }

    @Test
    void testDaysFromNullLlist() {
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,(List<ServiceOnVehicle>) null)).isEqualByComparingTo(0L);
    }

    @Test
    void testDaysFromEmptyList() {
        assertThat(serviceOnVehicleService.calculateTotalServiceDays(COMPANY_ID,List.of())).isEqualByComparingTo(0L);
    }

    private List<ServiceOnVehicle> buildServicesOneDayAheadToday() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(1L);
        LocalDate end = now.plusDays(1L);
        ServiceOnVehicle service = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start, end);
        return List.of(service);
    }

    private List<ServiceOnVehicle> buildServicesDisjoinedWithAllAheadDates() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(2L);
        // ahead date
        LocalDate end = now.plusDays(1L);

        // both ahead date
        LocalDate start2 = now.plusDays(2L);
        LocalDate end2 = now.plusDays(3L);

        ServiceOnVehicle service = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start, end);
        ServiceOnVehicle service2 = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start2, end2);
        return List.of(service, service2);
    }

    private List<ServiceOnVehicle> buildServicesOnVehicleJoinedAheadDates() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(2L);
        // ahead date
        LocalDate end = now.plusDays(3L);

        // ahead dates
        LocalDate start2 = now.plusDays(2L);
        LocalDate end2 = now.plusDays(4L);

        ServiceOnVehicle service = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start, end);
        ServiceOnVehicle service2 = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start2, end2);
        return List.of(service, service2);
    }

    private List<ServiceOnVehicle> buildWithFinishedServices() {
        LocalDate now = LocalDate.now();
        // two day difference
        LocalDate start = now.minusDays(10L);
        LocalDate end = now.minusDays(8L);

        ServiceOnVehicle service = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start, end);
        return List.of(service);
    }

    private List<ServiceOnVehicle> buildWithFinishedServicesWithJoinedDates() {
        LocalDate now = LocalDate.now();
        // ten day difference
        LocalDate start = now.minusDays(30L);
        LocalDate end = now.minusDays(20L);

        //ten days difference
        LocalDate start2 = now.minusDays(25L);
        LocalDate end2 = now.minusDays(15L);

        ServiceOnVehicle service = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start, end);
        ServiceOnVehicle service2 = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start2, end2);
        return List.of(service, service2);
    }

    private List<ServiceOnVehicle> buildServiceWithNoEndDate() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(10L);

        ServiceOnVehicle service = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start, null);
        return List.of(service);
    }

    private List<ServiceOnVehicle> buildTwoServiceWithNoEndDate() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(10L);
        LocalDate start2 = now.minusDays(25L);

        ServiceOnVehicle service = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start, null);
        ServiceOnVehicle service2 = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start2, null);
        return List.of(service, service2);
    }

    private List<ServiceOnVehicle> buildTwoServiceWithOnlyOneWithNoEndDate() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(10L);
        LocalDate end = now.minusDays(8L);

        LocalDate start2 = now.minusDays(30L);

        ServiceOnVehicle service = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start, end);
        ServiceOnVehicle service2 = new ServiceOnVehicle(serviceId, COMPANY_ID, vehicleId, start2, null);
        return List.of(service, service2);
    }
}
