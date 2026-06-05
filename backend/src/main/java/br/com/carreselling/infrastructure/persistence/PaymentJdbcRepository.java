package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.application.service.model.MonthlyPaymentTotal;
import br.com.carreselling.domain.model.Payment;
import br.com.carreselling.domain.model.PaymentType;
import br.com.carreselling.domain.repository.PaymentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentJdbcRepository implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Payment savePayment(Payment payment) {
        jdbcTemplate.update("""
                        INSERT INTO payments
                        (id, payment_type, description, amount, payment_date, vehicle_id,
                         notes, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                payment.id.toString(),
                payment.paymentType.name(),
                payment.description,
                payment.amount,
                payment.paymentDate,
                payment.vehicleId == null ? null : payment.vehicleId.toString(),
                payment.notes,
                Timestamp.from(payment.createdAt),
                payment.updatedAt == null ? null : Timestamp.from(payment.updatedAt)
        );
        return payment;
    }

    @Override
    public Optional<Payment> findPaymentById(UUID id) {
        List<Payment> result = jdbcTemplate.query("""
                        SELECT p.*, v.license_plate
                        FROM payments p
                        LEFT JOIN vehicles v ON v.id = p.vehicle_id
                        WHERE p.id = ?
                        """,
                new PaymentRowMapper(),
                id.toString());
        return result.stream().findFirst();
    }

    @Override
    public List<Payment> findPayments(PaymentType paymentType, Integer paymentYear, Integer paymentMonth, String licensePlate) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT p.*, v.license_plate
                FROM payments p
                LEFT JOIN vehicles v ON v.id = p.vehicle_id
                WHERE 1=1
                """);

        if (paymentType != null) {
            sql.append(" AND p.payment_type = ?");
            params.add(paymentType.name());
        }
        if (paymentYear != null) {
            sql.append(" AND YEAR(p.payment_date) = ?");
            params.add(paymentYear);
        }
        if (paymentMonth != null) {
            sql.append(" AND MONTH(p.payment_date) = ?");
            params.add(paymentMonth);
        }
        if (licensePlate != null && !licensePlate.isBlank()) {
            sql.append(" AND v.license_plate LIKE ?");
            params.add("%" + licensePlate + "%");
        }
        sql.append(" ORDER BY p.payment_date DESC, p.created_at DESC");

        return jdbcTemplate.query(sql.toString(), new PaymentRowMapper(), params.toArray());
    }

    @Override
    public List<String> findDistinctDescriptions(PaymentType paymentType) {
        return jdbcTemplate.queryForList("""
                        SELECT DISTINCT description
                        FROM payments
                        WHERE payment_type = ?
                          AND description IS NOT NULL
                          AND TRIM(description) <> ''
                        ORDER BY description
                        """,
                String.class,
                paymentType.name());
    }

    @Override
    public Payment updatePayment(Payment payment) {
        jdbcTemplate.update("""
                        UPDATE payments
                        SET payment_type = ?, description = ?, amount = ?, payment_date = ?,
                            vehicle_id = ?, notes = ?, updated_at = ?
                        WHERE id = ?
                        """,
                payment.paymentType.name(),
                payment.description,
                payment.amount,
                payment.paymentDate,
                payment.vehicleId == null ? null : payment.vehicleId.toString(),
                payment.notes,
                Timestamp.from(Instant.now()),
                payment.id.toString()
        );
        return payment;
    }

    @Override
    public void deletePayment(UUID id) {
        jdbcTemplate.update("DELETE FROM payments WHERE id = ?", id.toString());
    }

    @Override
    public List<Payment> findPaymentsByVehicleId(UUID vehicleId) {
        return jdbcTemplate.query("""
                        SELECT p.*, v.license_plate
                        FROM payments p
                        LEFT JOIN vehicles v ON v.id = p.vehicle_id
                        WHERE p.vehicle_id = ?
                        ORDER BY p.payment_date DESC, p.created_at DESC
                        """,
                new PaymentRowMapper(),
                vehicleId.toString());
    }

    @Override
    public void deletePaymentsByVehicleId(UUID vehicleId) {
        jdbcTemplate.update("DELETE FROM payments WHERE vehicle_id = ?", vehicleId.toString());
    }

    @Override
    public BigDecimal findTotalPaymentsAmount(LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (startDate != null) {
            sql.append(" AND payment_date >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND payment_date <= ?");
            params.add(Date.valueOf(endDate));
        }
        BigDecimal result = jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, params.toArray());
        return result == null ? BigDecimal.ZERO : result;
    }

    @Override
    public List<MonthlyPaymentTotal> findMonthlyPaymentTotals(LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    YEAR(payment_date)  AS pay_year,
                    MONTH(payment_date) AS pay_month,
                    SUM(amount)         AS total
                FROM payments
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (startDate != null) {
            sql.append(" AND payment_date >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND payment_date <= ?");
            params.add(Date.valueOf(endDate));
        }
        sql.append(" GROUP BY YEAR(payment_date), MONTH(payment_date) ORDER BY pay_year, pay_month");
        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new MonthlyPaymentTotal(
                        rs.getInt("pay_year"),
                        rs.getInt("pay_month"),
                        rs.getBigDecimal("total")
                ),
                params.toArray()
        );
    }

    private static class PaymentRowMapper implements RowMapper<Payment> {

        @Override
        public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            PaymentType paymentType = PaymentType.valueOf(rs.getString("payment_type"));
            String description = rs.getString("description");
            BigDecimal amount = rs.getBigDecimal("amount");
            Date paymentDateSql = rs.getDate("payment_date");
            LocalDate paymentDate = paymentDateSql == null ? null : paymentDateSql.toLocalDate();
            String vehicleIdStr = rs.getString("vehicle_id");
            UUID vehicleId = vehicleIdStr == null ? null : UUID.fromString(vehicleIdStr);
            String notes = rs.getString("notes");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAtTs = rs.getTimestamp("updated_at");
            Instant updatedAt = updatedAtTs == null ? null : updatedAtTs.toInstant();
            Payment payment = new Payment(id, paymentType, description, amount, paymentDate,
                    vehicleId, notes, createdAt, updatedAt);
            String licensePlate = rs.getString("license_plate");
            if (licensePlate != null) {
                payment.vehicleLicensePlate = licensePlate;
            }
            return payment;
        }
    }
}
