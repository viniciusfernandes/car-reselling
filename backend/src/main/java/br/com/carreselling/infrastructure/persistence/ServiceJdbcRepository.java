package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.ServiceEntry;
import br.com.carreselling.domain.model.ServiceType;
import br.com.carreselling.domain.repository.ServiceRepository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceJdbcRepository implements ServiceRepository {

    private final JdbcTemplate jdbcTemplate;

    public ServiceJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ServiceEntry saveService(ServiceEntry serviceEntry) {
        jdbcTemplate.update("""
                INSERT INTO services
                (id, vehicle_id, service_type, description, service_value, performed_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            serviceEntry.getId().toString(),
            serviceEntry.getVehicleId().toString(),
            serviceEntry.getServiceType().name(),
            serviceEntry.getDescription(),
            serviceEntry.getServiceValue(),
            serviceEntry.getPerformedAt(),
            Timestamp.from(serviceEntry.getCreatedAt()),
            serviceEntry.getUpdatedAt() == null ? null : Timestamp.from(serviceEntry.getUpdatedAt())
        );
        return serviceEntry;
    }

    @Override
    public Optional<ServiceEntry> findServiceById(UUID id) {
        List<ServiceEntry> result = jdbcTemplate.query("""
                SELECT * FROM services WHERE id = ?
                """,
            new ServiceRowMapper(),
            id.toString());
        return result.stream().findFirst();
    }

    @Override
    public List<ServiceEntry> findServiceByVehicleId(UUID vehicleId) {
        return jdbcTemplate.query("""
                SELECT * FROM services WHERE vehicle_id = ? ORDER BY created_at DESC
                """,
            new ServiceRowMapper(),
            vehicleId.toString());
    }

    @Override
    public ServiceEntry updateService(ServiceEntry serviceEntry) {
        jdbcTemplate.update("""
                UPDATE services
                SET service_type = ?, description = ?, service_value = ?, performed_at = ?, updated_at = ?
                WHERE id = ?
                """,
            serviceEntry.getServiceType().name(),
            serviceEntry.getDescription(),
            serviceEntry.getServiceValue(),
            serviceEntry.getPerformedAt(),
            serviceEntry.getUpdatedAt() == null ? Timestamp.from(Instant.now()) : Timestamp.from(serviceEntry.getUpdatedAt()),
            serviceEntry.getId().toString()
        );
        return serviceEntry;
    }

    @Override
    public void deleteService(UUID id) {
        jdbcTemplate.update("DELETE FROM services WHERE id = ?", id.toString());
    }

    @Override
    public BigDecimal findServiceTotalByVehicleId(UUID vehicleId) {
        BigDecimal total = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(service_value), 0) FROM services WHERE vehicle_id = ?
                """,
            new Object[]{vehicleId.toString()},
            BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    private static class ServiceRowMapper implements RowMapper<ServiceEntry> {

        @Override
        public ServiceEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID vehicleId = UUID.fromString(rs.getString("vehicle_id"));
            ServiceType serviceType = ServiceType.valueOf(rs.getString("service_type"));
            String description = rs.getString("description");
            BigDecimal value = rs.getBigDecimal("service_value");
            LocalDate performedAt = rs.getDate("performed_at") == null ? null : rs.getDate("performed_at").toLocalDate();
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new ServiceEntry(
                id,
                vehicleId,
                serviceType,
                description,
                value,
                performedAt,
                createdAt,
                updatedAt == null ? null : updatedAt.toInstant()
            );
        }
    }
}
