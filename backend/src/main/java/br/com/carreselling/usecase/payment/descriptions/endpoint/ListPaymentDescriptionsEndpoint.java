package br.com.carreselling.usecase.payment.descriptions.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.domain.model.PaymentType;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class ListPaymentDescriptionsEndpoint {

    private final IPaymentService paymentService;

    public ListPaymentDescriptionsEndpoint(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/descriptions")
    public ResponseEntity<ApiResponse<List<String>>> list(
            @RequestParam(required = false) PaymentType paymentType) {
        List<String> descriptions = paymentService.listDescriptions(paymentType);
        return ResponseEntity.ok(new ApiResponse<>(descriptions));
    }
}
