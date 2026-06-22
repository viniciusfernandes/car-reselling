package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.VehicleOnServiceHistory;
import br.com.carreselling.domain.repository.VehicleOnServiceHistoryRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleOnServiceHistoryJdbcRepository implements VehicleOnServiceHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public VehicleOnServiceHistoryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(int companyId, VehicleOnServiceHistory history) {
        jdbcTemplate.update("""
                        INSERT INTO vehicle_on_service_history (id, company_id, vehicle_id, on_service, changed_at)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                history.id().toString(),
                companyId,
                history.vehicleId().toString(),
                history.onService(),
                Timestamp.from(history.changedAt())
        );
    }

    @Override
    public List<VehicleOnServiceHistory> findByVehicleId(int companyId, UUID vehicleId) {
        return jdbcTemplate.query("""
                        SELECT id, company_id, vehicle_id, on_service, changed_at
                        FROM vehicle_on_service_history
                        WHERE vehicle_id = ? AND company_id = ?
                        ORDER BY changed_at ASC
                        """,
                new HistoryRowMapper(),
                vehicleId.toString(),
                companyId
        );
    }

    @Override
    public void deleteByVehicleId(int companyId, UUID vehicleId) {
        jdbcTemplate.update(
                "DELETE FROM vehicle_on_service_history WHERE vehicle_id = ? AND company_id = ?",
                vehicleId.toString(),
                companyId
        );
    }

    private static class HistoryRowMapper implements RowMapper<VehicleOnServiceHistory> {

        @Override
        public VehicleOnServiceHistory mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return new VehicleOnServiceHistory(
                    UUID.fromString(rs.getString("id")),
                    rs.getInt("company_id"),
                    UUID.fromString(rs.getString("vehicle_id")),
                    rs.getBoolean("on_service"),
                    rs.getTimestamp("changed_at").toInstant()
            );
        }
    }
}
