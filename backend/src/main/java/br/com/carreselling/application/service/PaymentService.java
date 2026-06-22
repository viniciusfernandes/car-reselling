package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.PaymentDocumentSummary;
import br.com.carreselling.application.service.model.PaymentSummary;
import br.com.carreselling.common.UuidGenerator;
import br.com.carreselling.domain.exception.NotFoundException;
import br.com.carreselling.domain.model.*;
import br.com.carreselling.domain.repository.PaymentDocumentRepository;
import br.com.carreselling.domain.repository.PaymentRepository;
import br.com.carreselling.domain.repository.VehicleRepository;
import br.com.carreselling.infrastructure.storage.PaymentDocumentStorage;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService implements IPaymentService {

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;

    private final PaymentRepository paymentRepository;
    private final PaymentDocumentRepository paymentDocumentRepository;
    private final PaymentDocumentStorage paymentDocumentStorage;
    private final VehicleRepository vehicleRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentDocumentRepository paymentDocumentRepository,
                          PaymentDocumentStorage paymentDocumentStorage,
                          VehicleRepository vehicleRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentDocumentRepository = paymentDocumentRepository;
        this.paymentDocumentStorage = paymentDocumentStorage;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public UUID createPayment(int companyId,
                              PaymentType paymentType,
                              String description,
                              BigDecimal amount,
                              LocalDate paymentDate,
                              String vehicleLicensePlate,
                              String notes) {
        UUID resolvedVehicleId = resolveVehicleForWarranty(companyId, paymentType, vehicleLicensePlate);
        String normalizedDescription = description != null ? description.toUpperCase().trim() : null;
        UUID id = UuidGenerator.generate();
        Payment payment = new Payment(id, companyId, paymentType, normalizedDescription, amount, paymentDate,
                resolvedVehicleId, notes, Instant.now(), null);
        paymentRepository.savePayment(companyId, payment);
        return id;
    }

    @Override
    public List<PaymentSummary> listPayments(int companyId, PaymentType paymentType, Integer paymentYear, Integer paymentMonth, String licensePlate) {
        return paymentRepository.findPayments(companyId, paymentType, paymentYear, paymentMonth, licensePlate)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public PaymentSummary getPayment(int companyId, UUID id) {
        Payment payment = paymentRepository.findPaymentById(companyId, id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return toSummary(payment);
    }

    @Override
    public void updatePayment(int companyId,
                              UUID id,
                              PaymentType paymentType,
                              String description,
                              BigDecimal amount,
                              LocalDate paymentDate,
                              String vehicleLicensePlate,
                              String notes) {
        Payment payment = paymentRepository.findPaymentById(companyId, id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        UUID resolvedVehicleId = resolveVehicleForWarranty(companyId, paymentType, vehicleLicensePlate);
        String normalizedDescription = description != null ? description.toUpperCase().trim() : null;
        payment.update(paymentType, normalizedDescription, amount, paymentDate, resolvedVehicleId, notes);
        paymentRepository.updatePayment(companyId, payment);
    }

    @Override
    public List<String> listDescriptions(int companyId, PaymentType paymentType) {
        return paymentRepository.findDistinctDescriptions(companyId, paymentType);
    }

    private UUID resolveVehicleForWarranty(int companyId, PaymentType paymentType, String vehicleLicensePlate) {
        if (paymentType != PaymentType.WARRANTY) {
            return null;
        }
        if (vehicleLicensePlate == null || vehicleLicensePlate.isBlank()) {
            throw new IllegalArgumentException("Vehicle license plate is required for WARRANTY payments.");
        }
        Vehicle vehicle = vehicleRepository.findVehicleByLicensePlate(companyId, vehicleLicensePlate.toUpperCase().trim())
                .orElseThrow(() -> new NotFoundException(
                        "Vehicle with plate '" + vehicleLicensePlate + "' not found."));
        if (vehicle.getStatus() != VehicleStatus.SOLD) {
            throw new IllegalArgumentException(
                    "Vehicle with plate '" + vehicleLicensePlate + "' is not sold. Only sold vehicles can have warranty entries.");
        }
        return vehicle.getId();
    }

    @Override
    public void deletePayment(int companyId, UUID id) {
        Payment payment = paymentRepository.findPaymentById(companyId, id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        List<PaymentDocument> documents = paymentDocumentRepository.findPaymentDocumentsByPaymentId(companyId, payment.id);
        for (PaymentDocument doc : documents) {
            paymentDocumentStorage.delete(doc.storageKey);
        }
        paymentDocumentRepository.deletePaymentDocumentsByPaymentId(companyId, id);
        paymentRepository.deletePayment(companyId, id);
    }

    @Override
    public void deletePaymentsByVehicleId(int companyId, UUID vehicleId) {
        List<Payment> payments = paymentRepository.findPaymentsByVehicleId(companyId, vehicleId);
        for (Payment payment : payments) {
            List<PaymentDocument> documents = paymentDocumentRepository.findPaymentDocumentsByPaymentId(companyId, payment.id);
            for (PaymentDocument doc : documents) {
                paymentDocumentStorage.delete(doc.storageKey);
            }
            paymentDocumentRepository.deletePaymentDocumentsByPaymentId(companyId, payment.id);
        }
        paymentRepository.deletePaymentsByVehicleId(companyId, vehicleId);
    }

    @Override
    public UUID uploadPaymentDocument(int companyId, UUID paymentId, MultipartFile file) {
        paymentRepository.findPaymentById(companyId, paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds maximum size.");
        }
        UUID documentId = UuidGenerator.generate();
        String originalFileName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
        String storageKey;
        try {
            storageKey = paymentDocumentStorage.store(paymentId, documentId, originalFileName, file.getInputStream());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store payment document");
        }
        PaymentDocument document = new PaymentDocument(
                documentId,
                companyId,
                paymentId,
                originalFileName,
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getSize(),
                storageKey,
                Instant.now(),
                "system"
        );
        paymentDocumentRepository.savePaymentDocument(companyId, document);
        return documentId;
    }

    @Override
    public List<PaymentDocumentSummary> listPaymentDocuments(int companyId, UUID paymentId) {
        paymentRepository.findPaymentById(companyId, paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return paymentDocumentRepository.findPaymentDocumentsByPaymentId(companyId, paymentId)
                .stream()
                .map(doc -> new PaymentDocumentSummary(
                        doc.id,
                        doc.paymentId,
                        doc.originalFileName,
                        doc.contentType,
                        doc.sizeBytes,
                        doc.uploadedAt
                ))
                .toList();
    }

    @Override
    public Resource downloadPaymentDocument(int companyId, UUID paymentId, UUID documentId) {
        PaymentDocument document = getPaymentDocumentEntity(companyId, paymentId, documentId);
        return paymentDocumentStorage.load(document.storageKey);
    }

    @Override
    public void deletePaymentDocument(int companyId, UUID paymentId, UUID documentId) {
        PaymentDocument document = getPaymentDocumentEntity(companyId, paymentId, documentId);
        paymentDocumentStorage.delete(document.storageKey);
        paymentDocumentRepository.deletePaymentDocument(companyId, documentId);
    }

    private PaymentDocument getPaymentDocumentEntity(int companyId, UUID paymentId, UUID documentId) {
        PaymentDocument document = paymentDocumentRepository.findPaymentDocumentById(companyId, documentId)
                .orElseThrow(() -> new NotFoundException("Payment document not found"));
        if (!document.paymentId.equals(paymentId)) {
            throw new NotFoundException("Document not found for payment");
        }
        return document;
    }

    private PaymentSummary toSummary(Payment payment) {
        return new PaymentSummary(
                payment.id,
                payment.paymentType,
                payment.description,
                payment.amount,
                payment.paymentDate,
                payment.vehicleId,
                payment.vehicleLicensePlate,
                payment.notes,
                payment.createdAt,
                payment.updatedAt
        );
    }
}
