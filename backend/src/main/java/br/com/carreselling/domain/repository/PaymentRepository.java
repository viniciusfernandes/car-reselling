package br.com.carreselling.domain.repository;

import br.com.carreselling.application.service.model.MonthlyPaymentTotal;
import br.com.carreselling.domain.model.Payment;
import br.com.carreselling.domain.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment savePayment(Payment payment);

    Optional<Payment> findPaymentById(UUID id);

    List<Payment> findPayments(PaymentType paymentType, Integer paymentYear, Integer paymentMonth, String licensePlate);

    List<String> findDistinctDescriptions(PaymentType paymentType);

    Payment updatePayment(Payment payment);

    void deletePayment(UUID id);

    List<Payment> findPaymentsByVehicleId(UUID vehicleId);

    void deletePaymentsByVehicleId(UUID vehicleId);

    BigDecimal findTotalPaymentsAmount(LocalDate startDate, LocalDate endDate);

    List<MonthlyPaymentTotal> findMonthlyPaymentTotals(LocalDate startDate, LocalDate endDate);
}
