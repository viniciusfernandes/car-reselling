package br.com.carreselling.usecase.payment.create.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.payment.create.contract.CreatePaymentRequest;
import br.com.carreselling.usecase.payment.create.contract.CreatePaymentResponse;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@Validated
public class CreatePaymentEndpoint {

    private final IPaymentService paymentService;

    public CreatePaymentEndpoint(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> create(@Valid @RequestBody CreatePaymentRequest request) {
        UUID id = paymentService.createPayment(
                request.paymentType(),
                request.description(),
                request.amount(),
                request.paymentDate(),
                request.vehicleLicensePlate(),
                request.notes()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(new CreatePaymentResponse(id)));
    }
}
