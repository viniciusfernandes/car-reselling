package br.com.carreselling.usecase.report.dashboard.endpoint;

import br.com.carreselling.application.service.IDashboardService;
import br.com.carreselling.application.service.model.FinancialDashboard;
import br.com.carreselling.config.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class FinancialDashboardEndpoint {

    private final IDashboardService dashboardService;

    public FinancialDashboardEndpoint(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/financial-dashboard")
    public ApiResponse<FinancialDashboard> getFinancialDashboard(
            @RequestParam(required = false, defaultValue = "0") BigDecimal cashBase
    ) {
        return new ApiResponse<>(dashboardService.getFinancialDashboard(cashBase));
    }
}
