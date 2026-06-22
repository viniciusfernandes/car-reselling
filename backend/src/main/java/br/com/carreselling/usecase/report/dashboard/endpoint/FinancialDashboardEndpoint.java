package br.com.carreselling.usecase.report.dashboard.endpoint;

import br.com.carreselling.application.service.IDashboardService;
import br.com.carreselling.application.service.model.FinancialDashboard;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class FinancialDashboardEndpoint {

    private final IDashboardService dashboardService;

    private final TenantContext tenantContext;

    public FinancialDashboardEndpoint(IDashboardService dashboardService, TenantContext tenantContext) {
        this.dashboardService = dashboardService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/financial-dashboard")
    public ApiResponse<FinancialDashboard> getFinancialDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        int companyId = tenantContext.getCurrentCompanyId();

        return new ApiResponse<>(dashboardService.getFinancialDashboard(companyId, startDate, endDate));
    }
}
