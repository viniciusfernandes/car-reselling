package br.com.carreselling.usecase.payment.update.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.payment.update.contract.UpdatePaymentRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@Validated
public class UpdatePaymentEndpoint {

    private final IPaymentService paymentService;

    private final TenantContext tenantContext;

    public UpdatePaymentEndpoint(IPaymentService paymentService, TenantContext tenantContext) {
        this.paymentService = paymentService;
        this.tenantContext = tenantContext;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdatePaymentRequest request) {
        int companyId = tenantContext.getCurrentCompanyId();
        paymentService.updatePayment(companyId, 
            id,
            request.paymentType(),
            request.description(),
            request.amount(),
            request.paymentDate(),
            request.vehicleLicensePlate(),
            request.notes()
        );
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}
