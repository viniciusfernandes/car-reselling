package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.carreselling.domain.repository.VehicleRepository;
import org.springframework.stereotype.Service;

@Service
public class SalesDashboardService implements IReportService {
    private final VehicleRepository vehicleRepository;
    private final VehicleSalesCalculator salesCalculator;

    public SalesDashboardService(VehicleRepository vehicleRepository, VehicleSalesCalculator salesCalculator) {
        this.vehicleRepository = vehicleRepository;
        this.salesCalculator = salesCalculator;
    }

    @Override
    public DistributedVehiclesReport distributedVehiclesReport(int companyId, DistributedVehiclesFilter filter) {
        List<DistribuitedVehicle> distributedVehicles = vehicleRepository.distributedVehiclesReport(companyId, filter);

        Map<UUID, PartnerAccumulator> grouped = new LinkedHashMap<>();
        for (DistribuitedVehicle row : distributedVehicles) {
            grouped.computeIfAbsent(row.partnerId(), key -> new PartnerAccumulator(row.partnerId(), row.partnerName()));
            PartnerAccumulator accumulator = grouped.get(row.partnerId());
            BigDecimal totalCost = row.purchasePrice()
                    .add(row.freightCost())
                    .add(row.servicesTotal());
            accumulator.vehicles.add(new ReportVehicleItem(
                    row.vehicleId(),
                    row.licensePlate(),
                    row.brand(),
                    row.model(),
                    row.year(),
                    row.distributedAt(),
                    row.purchasePrice(),
                    row.purchaseCommission(),
                    totalCost
            ));
            accumulator.totalValue = accumulator.totalValue.add(row.purchasePrice());
        }

        List<ReportPartnerGroup> partners = new ArrayList<>();
        int overallCount = 0;
        BigDecimal overallTotal = BigDecimal.ZERO;
        for (PartnerAccumulator accumulator : grouped.values()) {
            int count = accumulator.vehicles.size();
            partners.add(new ReportPartnerGroup(
                    accumulator.partnerId,
                    accumulator.partnerName,
                    accumulator.vehicles,
                    accumulator.totalValue,
                    count
            ));
            overallCount += count;
            overallTotal = overallTotal.add(accumulator.totalValue);
        }
        return new DistributedVehiclesReport(partners, overallCount, overallTotal);
    }

    @Override
    public SoldVehiclesReport soldVehiclesReport(int companyId, DistributedVehiclesFilter filter) {
        List<SoldVehicle> soldVehicles = vehicleRepository.findTotalServicesFromSoldVehicles(companyId, filter);
        return salesCalculator.buildSoldVehicles(soldVehicles);
    }


    private static class PartnerAccumulator {
        private final UUID partnerId;
        private final String partnerName;
        private final List<ReportVehicleItem> vehicles = new ArrayList<>();
        private BigDecimal totalValue = BigDecimal.ZERO;

        private PartnerAccumulator(UUID partnerId, String partnerName) {
            this.partnerId = partnerId;
            this.partnerName = partnerName;
        }
    }
}
