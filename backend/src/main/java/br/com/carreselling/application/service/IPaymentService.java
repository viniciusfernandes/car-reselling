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

    UUID createPayment(int companyId,
                       PaymentType paymentType,
                       String description,
                       BigDecimal amount,
                       LocalDate paymentDate,
                       String vehicleLicensePlate,
                       String notes);

    List<PaymentSummary> listPayments(int companyId, PaymentType paymentType, Integer paymentYear, Integer paymentMonth, String licensePlate);

    PaymentSummary getPayment(int companyId, UUID id);

    void updatePayment(int companyId,
                       UUID id,
                       PaymentType paymentType,
                       String description,
                       BigDecimal amount,
                       LocalDate paymentDate,
                       String vehicleLicensePlate,
                       String notes);

    void deletePayment(int companyId, UUID id);

    void deletePaymentsByVehicleId(int companyId, UUID vehicleId);

    List<String> listDescriptions(int companyId, PaymentType paymentType);

    UUID uploadPaymentDocument(int companyId, UUID paymentId, MultipartFile file);

    List<PaymentDocumentSummary> listPaymentDocuments(int companyId, UUID paymentId);

    Resource downloadPaymentDocument(int companyId, UUID paymentId, UUID documentId);

    void deletePaymentDocument(int companyId, UUID paymentId, UUID documentId);
}
