package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.ServiceOnVehicle;
import br.com.carreselling.domain.model.ServiceType;
import br.com.carreselling.domain.repository.ServiceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServiceJdbcRepository implements ServiceRepository {

    private final JdbcTemplate jdbcTemplate;

    public ServiceJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ServiceOnVehicle saveService(int companyId, ServiceOnVehicle serviceEntry) {
        jdbcTemplate.update("""
                        INSERT INTO services
                        (id, company_id, vehicle_id, service_type, description, service_value, start_date, end_date, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                serviceEntry.getId().toString(),
                companyId,
                serviceEntry.getVehicleId().toString(),
                serviceEntry.getServiceType().name(),
                serviceEntry.getDescription(),
                serviceEntry.getServiceValue(),
                serviceEntry.getStartDate(),
                serviceEntry.getEndDate(),
                Timestamp.from(serviceEntry.getCreatedAt()),
                serviceEntry.getUpdatedAt() == null ? null : Timestamp.from(serviceEntry.getUpdatedAt())
        );
        return serviceEntry;
    }

    @Override
    public Optional<ServiceOnVehicle> findServiceById(int companyId, UUID id) {
        List<ServiceOnVehicle> result = jdbcTemplate.query("""
                        SELECT * FROM services WHERE id = ? AND company_id = ?
                        """,
                new ServiceRowMapper(),
                id.toString(),
                companyId);
        return result.stream().findFirst();
    }

    @Override
    public List<ServiceOnVehicle> findServiceByVehicleId(int companyId, UUID vehicleId) {
        return jdbcTemplate.query("""
                        SELECT * FROM services WHERE vehicle_id = ? AND company_id = ? ORDER BY created_at DESC
                        """,
                new ServiceRowMapper(),
                vehicleId.toString(),
                companyId);
    }

    @Override
    public ServiceOnVehicle updateService(int companyId, ServiceOnVehicle serviceEntry) {
        jdbcTemplate.update("""
                        UPDATE services
                        SET service_type = ?, description = ?, service_value = ?,
                            start_date = ?, end_date = ?, updated_at = ?
                        WHERE id = ? AND company_id = ?
                        """,
                serviceEntry.getServiceType().name(),
                serviceEntry.getDescription(),
                serviceEntry.getServiceValue(),
                serviceEntry.getStartDate(),
                serviceEntry.getEndDate(),
                serviceEntry.getUpdatedAt() == null ? Timestamp.from(Instant.now()) : Timestamp.from(serviceEntry.getUpdatedAt()),
                serviceEntry.getId().toString(),
                companyId
        );
        return serviceEntry;
    }

    @Override
    public void deleteService(int companyId, UUID id) {
        jdbcTemplate.update("DELETE FROM services WHERE id = ? AND company_id = ?", id.toString(), companyId);
    }

    @Override
    public BigDecimal findServiceTotalByVehicleId(int companyId, UUID vehicleId) {
        BigDecimal total = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(SUM(service_value), 0) FROM services WHERE vehicle_id = ? AND company_id = ?
                        """,
                new Object[]{vehicleId.toString(), companyId},
                BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public void deleteServicesByVehicleId(int companyId, UUID vehicleId) {
        jdbcTemplate.update("DELETE FROM services WHERE vehicle_id = ? AND company_id = ?", vehicleId.toString(), companyId);
    }

    @Override
    public boolean existsOpenServiceByVehicleId(int companyId, UUID vehicleId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM services WHERE vehicle_id = ? AND company_id = ? AND (end_date IS NULL OR end_date > CURRENT_DATE))",
                Boolean.class,
                vehicleId.toString(),
                companyId
        ));
    }

    private static class ServiceRowMapper implements RowMapper<ServiceOnVehicle> {

        @Override
        public ServiceOnVehicle mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            int companyId = rs.getInt("company_id");
            UUID vehicleId = UUID.fromString(rs.getString("vehicle_id"));
            ServiceType serviceType = ServiceType.valueOf(rs.getString("service_type"));
            String description = rs.getString("description");
            BigDecimal value = rs.getBigDecimal("service_value");
            LocalDate startDate = rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate();
            LocalDate endDate = rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate();
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new ServiceOnVehicle(
                    id,
                    companyId,
                    vehicleId,
                    serviceType,
                    description,
                    value,
                    startDate,
                    endDate,
                    createdAt,
                    updatedAt == null ? null : updatedAt.toInstant()
            );
        }
    }
}
