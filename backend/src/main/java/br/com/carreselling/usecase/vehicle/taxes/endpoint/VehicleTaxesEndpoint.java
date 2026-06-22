package br.com.carreselling.usecase.vehicle.taxes.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.application.service.model.VehicleTaxes;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.vehicle.taxes.contract.VehicleTaxesResponse;
import br.com.carreselling.usecase.vehicle.taxes.mapping.VehicleTaxesMapper;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class VehicleTaxesEndpoint {

    private final IVehicleService vehicleService;

    private final TenantContext tenantContext;

    public VehicleTaxesEndpoint(IVehicleService vehicleService, TenantContext tenantContext) {
        this.vehicleService = vehicleService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{vehicleId}/taxes")
    public ApiResponse<VehicleTaxesResponse> getTaxes(@PathVariable UUID vehicleId) {
        int companyId = tenantContext.getCurrentCompanyId();
        VehicleTaxes taxes = vehicleService.getVehicleTaxes(companyId, vehicleId);
        return new ApiResponse<>(VehicleTaxesMapper.toResponse(taxes));
    }
}
