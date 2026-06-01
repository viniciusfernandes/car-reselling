package br.com.carreselling.domain.repository;

import br.com.carreselling.application.service.model.MonthlyPaymentTotal;
import br.com.carreselling.domain.model.Payment;
import br.com.carreselling.domain.model.PaymentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment savePayment(Payment payment);

    Optional<Payment> findPaymentById(UUID id);

    List<Payment> findPayments(PaymentType paymentType, Integer referenceYear, Integer referenceMonth, String licensePlate);

    List<String> findDistinctDescriptions(PaymentType paymentType);

    Payment updatePayment(Payment payment);

    void deletePayment(UUID id);

    BigDecimal findTotalPaymentsAmount();

    List<MonthlyPaymentTotal> findMonthlyPaymentTotals();
}
