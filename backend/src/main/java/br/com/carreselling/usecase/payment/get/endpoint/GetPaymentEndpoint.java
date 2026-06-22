package br.com.carreselling.usecase.payment.get.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.payment.list.contract.PaymentItem;
import br.com.carreselling.usecase.payment.list.mapping.PaymentListMapper;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class GetPaymentEndpoint {

    private final IPaymentService paymentService;

    private final TenantContext tenantContext;

    public GetPaymentEndpoint(IPaymentService paymentService, TenantContext tenantContext) {
        this.paymentService = paymentService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentItem>> get(@PathVariable UUID id) {
        int companyId = tenantContext.getCurrentCompanyId();
        PaymentItem item = PaymentListMapper.toItem(paymentService.getPayment(companyId, id));
        return ResponseEntity.ok(new ApiResponse<>(item));
    }
}
