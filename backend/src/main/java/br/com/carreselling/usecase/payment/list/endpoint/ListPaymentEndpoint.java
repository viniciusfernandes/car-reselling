package br.com.carreselling.usecase.payment.list.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.domain.model.PaymentType;
import br.com.carreselling.usecase.payment.list.contract.PaymentListResponse;
import br.com.carreselling.usecase.payment.list.mapping.PaymentListMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class ListPaymentEndpoint {

    private final IPaymentService paymentService;

    public ListPaymentEndpoint(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaymentListResponse>> list(
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) String referenceMonth,
            @RequestParam(required = false) String licensePlate) {
        String[] referenceDates = referenceMonth != null ? referenceMonth.split("-") : null;
        Integer year = referenceDates != null ? Integer.parseInt(referenceDates[0]) : null;
        Integer month = referenceDates != null ? Integer.parseInt(referenceDates[1]) : null;
        var payments = paymentService.listPayments(paymentType, year, month, licensePlate)
                .stream()
                .map(PaymentListMapper::toItem)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(new PaymentListResponse(payments)));
    }
}
