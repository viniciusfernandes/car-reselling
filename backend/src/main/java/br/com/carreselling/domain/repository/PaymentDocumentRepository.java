package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.PaymentDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentDocumentRepository {

    PaymentDocument savePaymentDocument(PaymentDocument document);

    Optional<PaymentDocument> findPaymentDocumentById(UUID id);

    List<PaymentDocument> findPaymentDocumentsByPaymentId(UUID paymentId);

    void deletePaymentDocument(UUID id);

    void deletePaymentDocumentsByPaymentId(UUID paymentId);
}
