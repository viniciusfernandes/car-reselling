package br.com.carreselling.usecase.financial.cashbalance.update.endpoint;

import br.com.carreselling.application.service.ICashBalanceService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
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

    private final TenantContext tenantContext;

    public UpdateCashBalanceEndpoint(ICashBalanceService cashBalanceService, TenantContext tenantContext) {
        this.cashBalanceService = cashBalanceService;
        this.tenantContext = tenantContext;
    }

    @PutMapping("/cash-balance")
    public ApiResponse<Void> update(
            @Valid @RequestBody UpdateCashBalanceRequest request,
            Authentication authentication
    ) throws UseCaseException {
        int companyId = tenantContext.getCurrentCompanyId();
        String changedBy = authentication != null ? authentication.getName() : null;
        cashBalanceService.updateCashBalance(companyId, request.amount(), changedBy);
        return new ApiResponse<>(null);
    }
}
