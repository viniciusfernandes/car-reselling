package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.application.service.model.DistributedVehiclesFilter;
import br.com.carreselling.application.service.model.DistribuitedVehicle;
import br.com.carreselling.application.service.model.SoldVehicle;
import br.com.carreselling.domain.model.SupplierSource;
import br.com.carreselling.domain.model.Vehicle;
import br.com.carreselling.domain.model.VehicleStatus;
import br.com.carreselling.domain.repository.VehicleRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleJdbcRepository implements VehicleRepository {

    private final JdbcTemplate jdbcTemplate;

    public VehicleJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Vehicle saveVehicle(Vehicle vehicle) {
        jdbcTemplate.update("""
                        INSERT INTO vehicles
                        (id, license_plate, renavam, vin, year, color, model, brand, brand_id, model_id, supplier_source,
                         purchase_price, freight_cost, purchase_commission, selling_price, purchase_payment_receipt_document_id,
                         purchase_invoice_document_id, status, assigned_partner_id, distributed_at, sold_at,
                         sale_commission_rate, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                vehicle.getId().toString(),
                vehicle.getLicensePlate(),
                vehicle.getRenavam(),
                vehicle.getVin(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getModel(),
                vehicle.getBrand(),
                optionalUuid(vehicle.getBrandId()),
                optionalUuid(vehicle.getModelId()),
                vehicle.getSupplierSource().name(),
                vehicle.getPurchasePrice(),
                vehicle.getFreightCost(),
                vehicle.getPurchaseCommission(),
                vehicle.getSellingPrice(),
                optionalUuid(vehicle.getPurchasePaymentReceiptDocumentId()),
                optionalUuid(vehicle.getPurchaseInvoiceDocumentId()),
                vehicle.getStatus().name(),
                optionalUuid(vehicle.getAssignedPartnerId()),
                vehicle.getDistributedAt() == null ? null : Timestamp.from(vehicle.getDistributedAt()),
                vehicle.getSoldAt(),
                vehicle.getSaleCommissionRate(),
                Timestamp.from(vehicle.getCreatedAt()),
                vehicle.getUpdatedAt() == null ? null : Timestamp.from(vehicle.getUpdatedAt())
        );
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findVehicleById(UUID id) {
        List<Vehicle> result = jdbcTemplate.query("""
                        SELECT * FROM vehicles WHERE id = ?
                        """,
                new VehicleRowMapper(),
                id.toString());
        return result.stream().findFirst();
    }

    @Override
    public Optional<Vehicle> findVehicleByLicensePlate(String licensePlate) {
        List<Vehicle> result = jdbcTemplate.query("""
                        SELECT * FROM vehicles WHERE license_plate = ?
                        """,
                new VehicleRowMapper(),
                licensePlate);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Vehicle> findVehicleByRenavam(String renavam) {
        List<Vehicle> result = jdbcTemplate.query("""
                        SELECT * FROM vehicles WHERE renavam = ?
                        """,
                new VehicleRowMapper(),
                renavam);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Vehicle> findVehicleByVin(String vin) {
        List<Vehicle> result = jdbcTemplate.query("""
                        SELECT * FROM vehicles WHERE vin = ?
                        """,
                new VehicleRowMapper(),
                vin);
        return result.stream().findFirst();
    }

    @Override
    public List<Vehicle> findVehicleByFilter(VehicleStatus status, String query, Boolean onService, int offset, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM vehicles v WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (status != null) {
            sql.append("AND v.status = ? ");
            params.add(status.name());
        }
        if (onService != null) {
            if (Boolean.TRUE.equals(onService)) {
                sql.append("AND EXISTS (SELECT 1 FROM services s WHERE s.vehicle_id = v.id AND s.end_date IS NULL) ");
            } else {
                sql.append("AND NOT EXISTS (SELECT 1 FROM services s WHERE s.vehicle_id = v.id AND s.end_date IS NULL) ");
            }
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (v.license_plate LIKE ? OR v.model LIKE ? OR v.brand LIKE ?) ");
            String q = "%" + query + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        sql.append("ORDER BY v.created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);
        return jdbcTemplate.query(java.util.Objects.requireNonNull(sql.toString()),
                new VehicleRowMapper(),
                params.toArray(new Object[0]));
    }

    @Override
    public long countVehicleByFilter(VehicleStatus status, String query, Boolean onService) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM vehicles v WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (status != null) {
            sql.append("AND v.status = ? ");
            params.add(status.name());
        }
        if (onService != null) {
            if (Boolean.TRUE.equals(onService)) {
                sql.append("AND EXISTS (SELECT 1 FROM services s WHERE s.vehicle_id = v.id AND s.end_date IS NULL) ");
            } else {
                sql.append("AND NOT EXISTS (SELECT 1 FROM services s WHERE s.vehicle_id = v.id AND s.end_date IS NULL) ");
            }
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (v.license_plate LIKE ? OR v.model LIKE ? OR v.brand LIKE ?) ");
            String q = "%" + query + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        Long count = jdbcTemplate.queryForObject(
                java.util.Objects.requireNonNull(sql.toString()),
                Long.class,
                params.toArray(new Object[0])
        );
        return count == null ? 0L : count;
    }

    @Override
    public Vehicle updateVehicle(Vehicle vehicle) {
        jdbcTemplate.update("""
                        UPDATE vehicles
                        SET renavam = ?, vin = ?, year = ?, color = ?, model = ?, brand = ?, brand_id = ?, model_id = ?, supplier_source = ?,
                            purchase_price = ?, freight_cost = ?, purchase_commission = ?, selling_price = ?,
                            purchase_payment_receipt_document_id = ?, purchase_invoice_document_id = ?,
                            status = ?, assigned_partner_id = ?, distributed_at = ?, sold_at = ?,
                            sale_commission_rate = ?, updated_at = ?
                        WHERE id = ?
                        """,
                vehicle.getRenavam(),
                vehicle.getVin(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getModel(),
                vehicle.getBrand(),
                optionalUuid(vehicle.getBrandId()),
                optionalUuid(vehicle.getModelId()),
                vehicle.getSupplierSource().name(),
                vehicle.getPurchasePrice(),
                vehicle.getFreightCost(),
                vehicle.getPurchaseCommission(),
                vehicle.getSellingPrice(),
                optionalUuid(vehicle.getPurchasePaymentReceiptDocumentId()),
                optionalUuid(vehicle.getPurchaseInvoiceDocumentId()),
                vehicle.getStatus().name(),
                optionalUuid(vehicle.getAssignedPartnerId()),
                vehicle.getDistributedAt() == null ? null : Timestamp.from(vehicle.getDistributedAt()),
                vehicle.getSoldAt(),
                vehicle.getSaleCommissionRate(),
                vehicle.getUpdatedAt() == null ? Timestamp.from(Instant.now()) : Timestamp.from(vehicle.getUpdatedAt()),
                vehicle.getId().toString()
        );
        return vehicle;
    }

    @Override
    public void deleteVehicle(UUID id) {
        jdbcTemplate.update("DELETE FROM vehicles WHERE id = ?", id.toString());
    }

    @Override
    public BigDecimal findVehicleServicesTotalByVehicleId(UUID vehicleId) {
        BigDecimal total = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(SUM(service_value), 0) FROM services WHERE vehicle_id = ?
                        """,
                BigDecimal.class,
                vehicleId.toString());
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public int countVehicleDocumentsByVehicleId(UUID vehicleId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*) FROM documents WHERE vehicle_id = ?
                        """,
                Integer.class,
                vehicleId.toString());
        return count == null ? 0 : count;
    }

    @Override
    public List<DistribuitedVehicle> distributedVehiclesReport(DistributedVehiclesFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id AS partner_id,
                       p.name AS partner_name,
                       v.id AS vehicle_id,
                       v.license_plate,
                       v.brand,
                       v.model,
                       v.year,
                       DATE(COALESCE(v.distributed_at, v.updated_at)) AS distributed_at,
                       v.purchase_price,
                       COALESCE(v.purchase_commission, 0) AS purchase_commission,
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

        return jdbcTemplate.query(
                Objects.requireNonNull(sql.toString()),
                new ReportRowMapper(),
                params.toArray(new Object[0])
        );
    }

    @Override
    public List<SoldVehicle> soldVehiclesReport(DistributedVehiclesFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT v.id AS vehicle_id,
                       v.license_plate,
                       v.brand,
                       v.model,
                       v.year,
                       v.sold_at,
                       v.purchase_price,
                       COALESCE(v.purchase_commission, 0) AS purchase_commission,
                       v.freight_cost,
                       v.selling_price,
                       COALESCE(s.services_total, 0) AS services_total,
                       v.sale_commission_rate
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
                sql.append(" AND v.sold_at >= ?");
                params.add(Date.valueOf(startDate));
            }
            if (endDate != null) {
                sql.append(" AND v.sold_at <= ?");
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
        sql.append(" ORDER BY v.sold_at DESC, v.license_plate");

        return jdbcTemplate.query(
                java.util.Objects.requireNonNull(sql.toString()),
                new SoldVehicleMapper(),
                params.toArray(new Object[0])
        );
    }

    private static String optionalUuid(UUID id) {
        return id == null ? null : id.toString();
    }

    private static class SoldVehicleMapper implements RowMapper<SoldVehicle> {

        @Override
        public SoldVehicle mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            Date soldAtDate = rs.getDate("sold_at");
            return new SoldVehicle(
                    UUID.fromString(rs.getString("vehicle_id")),
                    rs.getString("license_plate"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getInt("year"),
                    soldAtDate != null ? soldAtDate.toLocalDate() : null,
                    rs.getBigDecimal("purchase_price"),
                    rs.getBigDecimal("purchase_commission"),
                    rs.getBigDecimal("freight_cost"),
                    rs.getBigDecimal("selling_price"),
                    rs.getBigDecimal("services_total"),
                    rs.getBigDecimal("sale_commission_rate")
            );
        }
    }

    private static class ReportRowMapper implements RowMapper<DistribuitedVehicle> {

        @Override
        public DistribuitedVehicle mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return new DistribuitedVehicle(
                    UUID.fromString(rs.getString("partner_id")),
                    rs.getString("partner_name"),
                    UUID.fromString(rs.getString("vehicle_id")),
                    rs.getString("license_plate"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getInt("year"),
                    rs.getDate("distributed_at") == null ? null : rs.getDate("distributed_at").toLocalDate(),
                    rs.getBigDecimal("purchase_price"),
                    rs.getBigDecimal("purchase_commission"),
                    rs.getBigDecimal("freight_cost"),
                    rs.getBigDecimal("services_total")
            );
        }
    }

    private static class VehicleRowMapper implements RowMapper<Vehicle> {

        @Override
        public Vehicle mapRow(@org.springframework.lang.NonNull ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            String licensePlate = rs.getString("license_plate");
            String renavam = rs.getString("renavam");
            String vin = rs.getString("vin");
            int year = rs.getInt("year");
            String color = rs.getString("color");
            String model = rs.getString("model");
            String brand = rs.getString("brand");
            UUID brandId = optionalUuid(rs.getString("brand_id"));
            UUID modelId = optionalUuid(rs.getString("model_id"));
            SupplierSource supplierSource = SupplierSource.valueOf(rs.getString("supplier_source"));
            BigDecimal purchasePrice = rs.getBigDecimal("purchase_price");
            BigDecimal freightCost = rs.getBigDecimal("freight_cost");
            BigDecimal purchaseCommission = rs.getBigDecimal("purchase_commission");
            BigDecimal sellingPrice = rs.getBigDecimal("selling_price");
            UUID paymentReceiptId = optionalUuid(rs.getString("purchase_payment_receipt_document_id"));
            UUID invoiceId = optionalUuid(rs.getString("purchase_invoice_document_id"));
            VehicleStatus status = VehicleStatus.valueOf(rs.getString("status"));
            UUID assignedPartnerId = optionalUuid(rs.getString("assigned_partner_id"));
            Timestamp distributedAt = rs.getTimestamp("distributed_at");
            java.sql.Date soldAtDate = rs.getDate("sold_at");
            BigDecimal saleCommissionRate = rs.getBigDecimal("sale_commission_rate");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new Vehicle(
                    id,
                    licensePlate,
                    renavam,
                    vin,
                    year,
                    color,
                    model,
                    brand,
                    brandId,
                    modelId,
                    supplierSource,
                    purchasePrice,
                    freightCost == null ? BigDecimal.ZERO : freightCost,
                    purchaseCommission == null ? BigDecimal.ZERO : purchaseCommission,
                    sellingPrice,
                    paymentReceiptId,
                    invoiceId,
                    status,
                    assignedPartnerId,
                    distributedAt == null ? null : distributedAt.toInstant(),
                    soldAtDate == null ? null : soldAtDate.toLocalDate(),
                    saleCommissionRate,
                    createdAt,
                    updatedAt == null ? null : updatedAt.toInstant()
            );
        }

        private static UUID optionalUuid(String value) {
            return value == null ? null : UUID.fromString(value);
        }
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
}
