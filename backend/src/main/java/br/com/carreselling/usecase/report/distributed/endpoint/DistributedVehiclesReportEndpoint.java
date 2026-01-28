package br.com.carreselling.usecase.report.distributed.endpoint;

import br.com.carreselling.application.service.IReportService;
import br.com.carreselling.application.service.model.DistributedVehiclesFilter;
import br.com.carreselling.application.service.model.DistributedVehiclesReport;
import br.com.carreselling.config.ApiResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<DistributedVehiclesReport> report(
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) String model,
        @RequestParam(required = false) UUID partnerId
    ) {
        DistributedVehiclesFilter filter = new DistributedVehiclesFilter(
            startDate,
            endDate,
            brand,
            model,
            partnerId
        );
        return new ApiResponse<>(reportService.distributedVehiclesReport(filter));
    }
}
