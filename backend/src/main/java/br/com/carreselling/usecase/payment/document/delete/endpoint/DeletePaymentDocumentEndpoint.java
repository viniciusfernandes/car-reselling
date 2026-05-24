package br.com.carreselling.usecase.payment.document.delete.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class DeletePaymentDocumentEndpoint {

    private final IPaymentService paymentService;

    public DeletePaymentDocumentEndpoint(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @DeleteMapping("/{paymentId}/documents/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID paymentId,
                                       @PathVariable UUID documentId) {
        paymentService.deletePaymentDocument(paymentId, documentId);
        return ResponseEntity.noContent().build();
    }
}
