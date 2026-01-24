package br.com.carreselling.usecase.report.distributed.endpoint;

import br.com.carreselling.application.service.IReportService;
import br.com.carreselling.application.service.model.DistributedVehiclesReport;
import br.com.carreselling.config.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class DistributedVehiclesReportEndpoint {

    private final IReportService reportService;

    public DistributedVehiclesReportEndpoint(IReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/distributed-vehicles")
    public ApiResponse<DistributedVehiclesReport> report() {
        return new ApiResponse<>(reportService.distributedVehiclesReport());
    }
}
