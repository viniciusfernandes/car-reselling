package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.PaymentDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentDocumentRepository {

    PaymentDocument savePaymentDocument(int companyId, PaymentDocument document);

    Optional<PaymentDocument> findPaymentDocumentById(int companyId, UUID id);

    List<PaymentDocument> findPaymentDocumentsByPaymentId(int companyId, UUID paymentId);

    void deletePaymentDocument(int companyId, UUID id);

    void deletePaymentDocumentsByPaymentId(int companyId, UUID paymentId);
}
