package br.com.carreselling.usecase.payment.delete.endpoint;

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
public class DeletePaymentEndpoint {

    private final IPaymentService paymentService;

    private final TenantContext tenantContext;

    public DeletePaymentEndpoint(IPaymentService paymentService, TenantContext tenantContext) {
        this.paymentService = paymentService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        int companyId = tenantContext.getCurrentCompanyId();
        paymentService.deletePayment(companyId, id);
        return ResponseEntity.noContent().build();
    }
}
