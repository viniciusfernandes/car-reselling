package br.com.carreselling.usecase.financial.cashbalance.get.endpoint;

import br.com.carreselling.application.service.ICashBalanceService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.domain.model.CashBalance;
import br.com.carreselling.usecase.financial.cashbalance.contract.CashBalanceResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financial")
@Validated
public class GetCashBalanceEndpoint {

    private final ICashBalanceService cashBalanceService;

    private final TenantContext tenantContext;

    public GetCashBalanceEndpoint(ICashBalanceService cashBalanceService, TenantContext tenantContext) {
        this.cashBalanceService = cashBalanceService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/cash-balance")
    public ApiResponse<CashBalanceResponse> get() {
        int companyId = tenantContext.getCurrentCompanyId();
        CashBalance cashBalance = cashBalanceService.getCashBalance(companyId);
        return new ApiResponse<>(new CashBalanceResponse(cashBalance.amount, cashBalance.updatedAt));
    }
}
