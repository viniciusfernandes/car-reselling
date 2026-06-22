package br.com.carreselling.usecase.vehicle.list.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.application.service.model.VehicleSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.domain.model.VehicleStatus;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.vehicle.list.contract.VehicleListItem;
import br.com.carreselling.usecase.vehicle.list.contract.VehicleListResponse;
import br.com.carreselling.usecase.vehicle.list.mapping.VehicleListMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class ListVehicleEndpoint {

    private final IVehicleService vehicleService;

    private final TenantContext tenantContext;

    public ListVehicleEndpoint(IVehicleService vehicleService, TenantContext tenantContext) {
        this.vehicleService = vehicleService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ApiResponse<VehicleListResponse> list(@RequestParam(required = false) VehicleStatus status,
                                                 @RequestParam(required = false) String q,
                                                 @RequestParam(required = false) Boolean onService,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        int companyId = tenantContext.getCurrentCompanyId();
        List<VehicleSummary> vehicles = vehicleService.listVehicles(companyId, status, q, onService, page, size);
        long total = vehicleService.countVehicles(companyId, status, q, onService);
        List<VehicleListItem> items = vehicles.stream()
                .map(VehicleListMapper::toItem)
                .toList();
        return new ApiResponse<>(new VehicleListResponse(items, page, size, total));
    }
}
