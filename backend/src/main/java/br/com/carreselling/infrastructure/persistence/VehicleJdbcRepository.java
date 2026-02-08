package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.SupplierSource;
import br.com.carreselling.domain.model.Vehicle;
import br.com.carreselling.domain.model.VehicleStatus;
import br.com.carreselling.domain.repository.VehicleRepository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
                (id, license_plate, renavam, vin, year, color, model, brand, supplier_source,
                 purchase_price, freight_cost, purchase_commission, selling_price, purchase_payment_receipt_document_id,
                 purchase_invoice_document_id, status, assigned_partner_id, distributed_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            vehicle.getId().toString(),
            vehicle.getLicensePlate(),
            vehicle.getRenavam(),
            vehicle.getVin(),
            vehicle.getYear(),
            vehicle.getColor(),
            vehicle.getModel(),
            vehicle.getBrand(),
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
    public List<Vehicle> findVehicleByFilter(VehicleStatus status, String query, int offset, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM vehicles WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status.name());
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (license_plate LIKE ? OR model LIKE ? OR brand LIKE ?) ");
            String q = "%" + query + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        sql.append("ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);
        return jdbcTemplate.query(java.util.Objects.requireNonNull(sql.toString()),
            new VehicleRowMapper(),
            params.toArray(new Object[0]));
    }

    @Override
    public long countVehicleByFilter(VehicleStatus status, String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM vehicles WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status.name());
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (license_plate LIKE ? OR model LIKE ? OR brand LIKE ?) ");
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
                SET renavam = ?, vin = ?, year = ?, color = ?, model = ?, brand = ?, supplier_source = ?,
                    purchase_price = ?, freight_cost = ?, purchase_commission = ?, selling_price = ?,
                    purchase_payment_receipt_document_id = ?, purchase_invoice_document_id = ?,
                    status = ?, assigned_partner_id = ?, distributed_at = ?, updated_at = ?
                WHERE id = ?
                """,
            vehicle.getRenavam(),
            vehicle.getVin(),
            vehicle.getYear(),
            vehicle.getColor(),
            vehicle.getModel(),
            vehicle.getBrand(),
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

    private static String optionalUuid(UUID id) {
        return id == null ? null : id.toString();
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
                createdAt,
                updatedAt == null ? null : updatedAt.toInstant()
            );
        }

        private static UUID optionalUuid(String value) {
            return value == null ? null : UUID.fromString(value);
        }
    }
}
