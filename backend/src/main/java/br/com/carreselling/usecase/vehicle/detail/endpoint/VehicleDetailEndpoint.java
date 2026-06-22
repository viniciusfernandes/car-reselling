package br.com.carreselling.usecase.vehicle.detail.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.application.service.model.VehicleDetail;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.vehicle.detail.contract.VehicleDetailResponse;
import br.com.carreselling.usecase.vehicle.detail.mapping.VehicleDetailMapper;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class VehicleDetailEndpoint {

    private final IVehicleService vehicleService;

    private final TenantContext tenantContext;

    public VehicleDetailEndpoint(IVehicleService vehicleService, TenantContext tenantContext) {
        this.vehicleService = vehicleService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{vehicleId}")
    public ApiResponse<VehicleDetailResponse> detail(@PathVariable UUID vehicleId) {
        int companyId = tenantContext.getCurrentCompanyId();
        VehicleDetail detail = vehicleService.getVehicle(companyId, vehicleId);
        return new ApiResponse<>(VehicleDetailMapper.toResponse(detail));
    }
}
