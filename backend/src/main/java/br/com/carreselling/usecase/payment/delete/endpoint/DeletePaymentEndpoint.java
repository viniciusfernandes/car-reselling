package br.com.carreselling.usecase.payment.delete.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class DeletePaymentEndpoint {

    private final IPaymentService paymentService;

    public DeletePaymentEndpoint(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
