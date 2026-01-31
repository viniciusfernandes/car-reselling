package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.DistributedVehiclesFilter;
import br.com.carreselling.application.service.model.DistributedVehiclesReport;
import br.com.carreselling.application.service.model.ReportPartnerGroup;
import br.com.carreselling.application.service.model.ReportVehicleItem;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ReportService implements IReportService {

    private final JdbcTemplate jdbcTemplate;
    private final VehicleSalesCalculator salesCalculator;

    public ReportService(JdbcTemplate jdbcTemplate, VehicleSalesCalculator salesCalculator) {
        this.jdbcTemplate = jdbcTemplate;
        this.salesCalculator = salesCalculator;
    }

    @Override
    public DistributedVehiclesReport distributedVehiclesReport(DistributedVehiclesFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id AS partner_id,
                       p.name AS partner_name,
                       v.id AS vehicle_id,
                       v.license_plate,
                       v.brand,
                       v.model,
                       v.year,
                       DATE(v.updated_at) AS distributed_at,
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
                """);

        List<Object> params = new ArrayList<>();

        if (filter != null) {
            LocalDate startDate = filter.startDate();
            LocalDate endDate = filter.endDate();
            String brand = normalizeText(filter.brand());
            String model = normalizeText(filter.model());
            if (startDate != null) {
                sql.append(" AND DATE(v.updated_at) >= ?");
                params.add(Date.valueOf(startDate));
            }
            if (endDate != null) {
                sql.append(" AND DATE(v.updated_at) <= ?");
                params.add(Date.valueOf(endDate));
            }
            if (brand != null) {
                sql.append(" AND UPPER(v.brand) LIKE ?");
                params.add("%" + brand + "%");
            }
            if (model != null) {
                sql.append(" AND UPPER(v.model) LIKE ?");
                params.add("%" + model + "%");
            }
            if (filter.partnerId() != null) {
                sql.append(" AND v.assigned_partner_id = ?");
                params.add(filter.partnerId().toString());
            }
        }

        sql.append(" ORDER BY p.name, v.license_plate");

        List<ReportRow> rows = jdbcTemplate.query(
            Objects.requireNonNull(sql.toString()),
            new ReportRowMapper(),
            params.toArray(new Object[0])
        );

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
                row.distributedAt(),
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

    @Override
    public br.com.carreselling.application.service.model.SoldVehiclesReport soldVehiclesReport(
        DistributedVehiclesFilter filter
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT v.id AS vehicle_id,
                       v.license_plate,
                       v.brand,
                       v.model,
                       v.year,
                       DATE(v.updated_at) AS sold_at,
                       v.purchase_price,
                       v.freight_cost,
                       v.selling_price,
                       COALESCE(s.services_total, 0) AS services_total
                FROM vehicles v
                LEFT JOIN (
                    SELECT vehicle_id, SUM(service_value) AS services_total
                    FROM services
                    GROUP BY vehicle_id
                ) s ON s.vehicle_id = v.id
                WHERE v.status = 'SOLD' AND v.selling_price IS NOT NULL
                """);

        List<Object> params = new ArrayList<>();
        if (filter != null) {
            LocalDate startDate = filter.startDate();
            LocalDate endDate = filter.endDate();
            String brand = normalizeText(filter.brand());
            String model = normalizeText(filter.model());
            if (startDate != null) {
                sql.append(" AND DATE(v.updated_at) >= ?");
                params.add(Date.valueOf(startDate));
            }
            if (endDate != null) {
                sql.append(" AND DATE(v.updated_at) <= ?");
                params.add(Date.valueOf(endDate));
            }
            if (brand != null) {
                sql.append(" AND UPPER(v.brand) LIKE ?");
                params.add("%" + brand + "%");
            }
            if (model != null) {
                sql.append(" AND UPPER(v.model) LIKE ?");
                params.add("%" + model + "%");
            }
            if (filter.partnerId() != null) {
                sql.append(" AND v.assigned_partner_id = ?");
                params.add(filter.partnerId().toString());
            }
        }
        sql.append(" ORDER BY v.updated_at DESC");

        List<VehicleSalesCalculator.SoldVehicleRaw> rows = jdbcTemplate.query(
            java.util.Objects.requireNonNull(sql.toString()),
            new SoldVehicleRowMapper(),
            params.toArray(new Object[0])
        );

        return salesCalculator.buildReport(rows);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase();
    }

    private record ReportRow(UUID partnerId,
                             String partnerName,
                             UUID vehicleId,
                             String licensePlate,
                             String brand,
                             String model,
                             int year,
                             LocalDate distributedAt,
                             BigDecimal purchasePrice,
                             BigDecimal freightCost,
                             BigDecimal servicesTotal) {
    }

    private static class SoldVehicleRowMapper implements RowMapper<VehicleSalesCalculator.SoldVehicleRaw> {

        @Override
        public VehicleSalesCalculator.SoldVehicleRaw mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            Date soldAtDate = rs.getDate("sold_at");
            return new VehicleSalesCalculator.SoldVehicleRaw(
                UUID.fromString(rs.getString("vehicle_id")),
                rs.getString("license_plate"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("year"),
                soldAtDate != null ? soldAtDate.toLocalDate() : null,
                rs.getBigDecimal("purchase_price"),
                rs.getBigDecimal("freight_cost"),
                rs.getBigDecimal("selling_price"),
                rs.getBigDecimal("services_total")
            );
        }
    }

    private static class ReportRowMapper implements RowMapper<ReportRow> {

        @Override
        public ReportRow mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return new ReportRow(
                UUID.fromString(rs.getString("partner_id")),
                rs.getString("partner_name"),
                UUID.fromString(rs.getString("vehicle_id")),
                rs.getString("license_plate"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getDate("distributed_at") == null ? null : rs.getDate("distributed_at").toLocalDate(),
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
