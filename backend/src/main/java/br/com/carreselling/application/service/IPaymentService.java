package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.PaymentDocumentSummary;
import br.com.carreselling.application.service.model.PaymentSummary;
import br.com.carreselling.domain.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IPaymentService {

    UUID createPayment(PaymentType paymentType,
                       String description,
                       BigDecimal amount,
                       LocalDate paymentDate,
                       String vehicleLicensePlate,
                       String notes);

    List<PaymentSummary> listPayments(PaymentType paymentType, Integer paymentYear, Integer paymentMonth, String licensePlate);

    PaymentSummary getPayment(UUID id);

    void updatePayment(UUID id,
                       PaymentType paymentType,
                       String description,
                       BigDecimal amount,
                       LocalDate paymentDate,
                       String vehicleLicensePlate,
                       String notes);

    void deletePayment(UUID id);

    void deletePaymentsByVehicleId(UUID vehicleId);

    List<String> listDescriptions(PaymentType paymentType);

    UUID uploadPaymentDocument(UUID paymentId, MultipartFile file);

    List<PaymentDocumentSummary> listPaymentDocuments(UUID paymentId);

    Resource downloadPaymentDocument(UUID paymentId, UUID documentId);

    void deletePaymentDocument(UUID paymentId, UUID documentId);
}
