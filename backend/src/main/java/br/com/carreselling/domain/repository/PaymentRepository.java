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

    Payment savePayment(int companyId, Payment payment);

    Optional<Payment> findPaymentById(int companyId, UUID id);

    List<Payment> findPayments(int companyId, PaymentType paymentType, Integer paymentYear, Integer paymentMonth, String licensePlate);

    List<String> findDistinctDescriptions(int companyId, PaymentType paymentType);

    Payment updatePayment(int companyId, Payment payment);

    void deletePayment(int companyId, UUID id);

    List<Payment> findPaymentsByVehicleId(int companyId, UUID vehicleId);

    void deletePaymentsByVehicleId(int companyId, UUID vehicleId);

    BigDecimal findTotalPaymentsAmount(int companyId, LocalDate startDate, LocalDate endDate);

    List<MonthlyPaymentTotal> findMonthlyPaymentTotals(int companyId, LocalDate startDate, LocalDate endDate);
}
