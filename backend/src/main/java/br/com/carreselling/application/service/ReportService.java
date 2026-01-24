package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.DistributedVehiclesReport;
import br.com.carreselling.application.service.model.ReportPartnerGroup;
import br.com.carreselling.application.service.model.ReportVehicleItem;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ReportService implements IReportService {

    private final JdbcTemplate jdbcTemplate;

    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DistributedVehiclesReport distributedVehiclesReport() {
        List<ReportRow> rows = jdbcTemplate.query("""
                SELECT p.id AS partner_id,
                       p.name AS partner_name,
                       v.id AS vehicle_id,
                       v.license_plate,
                       v.brand,
                       v.model,
                       v.year,
                       v.purchase_price,
                       v.freight_cost,
                       COALESCE(s.services_total, 0) AS services_total
                FROM vehicles v
                INNER JOIN partners p ON p.id = v.assigned_partner_id
                LEFT JOIN (
                    SELECT vehicle_id, SUM(service_value) AS services_total
                    FROM services
                    GROUP BY vehicle_id
                ) s ON s.vehicle_id = v.id
                WHERE v.status = 'DISTRIBUTED'
                ORDER BY p.name, v.license_plate
                """,
            new ReportRowMapper());

        Map<UUID, PartnerAccumulator> grouped = new LinkedHashMap<>();
        for (ReportRow row : rows) {
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
                row.purchasePrice(),
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

    private record ReportRow(UUID partnerId,
                             String partnerName,
                             UUID vehicleId,
                             String licensePlate,
                             String brand,
                             String model,
                             int year,
                             BigDecimal purchasePrice,
                             BigDecimal freightCost,
                             BigDecimal servicesTotal) {
    }

    private static class ReportRowMapper implements RowMapper<ReportRow> {

        @Override
        public ReportRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ReportRow(
                UUID.fromString(rs.getString("partner_id")),
                rs.getString("partner_name"),
                UUID.fromString(rs.getString("vehicle_id")),
                rs.getString("license_plate"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getBigDecimal("purchase_price"),
                rs.getBigDecimal("freight_cost"),
                rs.getBigDecimal("services_total")
            );
        }
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
