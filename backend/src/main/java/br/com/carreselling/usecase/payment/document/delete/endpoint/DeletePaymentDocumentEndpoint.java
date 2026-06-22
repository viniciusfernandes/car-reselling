package br.com.carreselling.usecase.payment.document.delete.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.carreselling.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/payments")
public class DeletePaymentDocumentEndpoint {

    private final IPaymentService paymentService;

    private final TenantContext tenantContext;

    public DeletePaymentDocumentEndpoint(IPaymentService paymentService, TenantContext tenantContext) {
        this.paymentService = paymentService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{paymentId}/documents/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID paymentId,
                                       @PathVariable UUID documentId) {
        int companyId = tenantContext.getCurrentCompanyId();
        paymentService.deletePaymentDocument(companyId, paymentId, documentId);
        return ResponseEntity.noContent().build();
    }
}
