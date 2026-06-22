package br.com.carreselling.usecase.payment.document.list.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
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

    private final TenantContext tenantContext;

    public ListPaymentDocumentEndpoint(IPaymentService paymentService, TenantContext tenantContext) {
        this.paymentService = paymentService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{paymentId}/documents")
    public ResponseEntity<ApiResponse<PaymentDocumentListResponse>> list(@PathVariable UUID paymentId) {
        int companyId = tenantContext.getCurrentCompanyId();
        List<PaymentDocumentItem> documents = paymentService.listPaymentDocuments(companyId, paymentId)
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
