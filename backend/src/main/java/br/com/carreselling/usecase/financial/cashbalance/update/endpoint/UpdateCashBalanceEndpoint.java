package br.com.carreselling.usecase.financial.cashbalance.update.endpoint;

import br.com.carreselling.application.service.ICashBalanceService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.UseCaseException;
import br.com.carreselling.usecase.financial.cashbalance.update.contract.UpdateCashBalanceRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financial")
@Validated
public class UpdateCashBalanceEndpoint {

    private final ICashBalanceService cashBalanceService;

    public UpdateCashBalanceEndpoint(ICashBalanceService cashBalanceService) {
        this.cashBalanceService = cashBalanceService;
    }

    @PutMapping("/cash-balance")
    public ApiResponse<Void> update(
            @Valid @RequestBody UpdateCashBalanceRequest request,
            Authentication authentication
    ) throws UseCaseException {
        String changedBy = authentication != null ? authentication.getName() : null;
        cashBalanceService.updateCashBalance(request.amount(), changedBy);
        return new ApiResponse<>(null);
    }
}
