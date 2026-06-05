package br.com.carreselling.usecase.payment.update.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
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

    public UpdatePaymentEndpoint(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdatePaymentRequest request) {
        paymentService.updatePayment(
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
