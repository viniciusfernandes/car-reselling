package br.com.carreselling.usecase.payment.document.list.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.payment.document.list.contract.PaymentDocumentItem;
import br.com.carreselling.usecase.payment.document.list.contract.PaymentDocumentListResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class ListPaymentDocumentEndpoint {

    private final IPaymentService paymentService;

    public ListPaymentDocumentEndpoint(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}/documents")
    public ResponseEntity<ApiResponse<PaymentDocumentListResponse>> list(@PathVariable UUID paymentId) {
        List<PaymentDocumentItem> documents = paymentService.listPaymentDocuments(paymentId)
            .stream()
            .map(doc -> new PaymentDocumentItem(
                doc.id(),
                doc.paymentId(),
                doc.originalFileName(),
                doc.contentType(),
                doc.sizeBytes(),
                doc.uploadedAt()
            ))
            .toList();
        return ResponseEntity.ok(new ApiResponse<>(new PaymentDocumentListResponse(documents)));
    }
}
