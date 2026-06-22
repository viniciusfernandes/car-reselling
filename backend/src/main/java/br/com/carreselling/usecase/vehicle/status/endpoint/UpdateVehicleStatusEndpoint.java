package br.com.carreselling.usecase.vehicle.status.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.vehicle.status.contract.UpdateVehicleStatusRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class UpdateVehicleStatusEndpoint {

    private final IVehicleService vehicleService;

    private final TenantContext tenantContext;

    public UpdateVehicleStatusEndpoint(IVehicleService vehicleService, TenantContext tenantContext) {
        this.vehicleService = vehicleService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{vehicleId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID vehicleId,
                                          @Valid @RequestBody UpdateVehicleStatusRequest request) {
        int companyId = tenantContext.getCurrentCompanyId();
        vehicleService.transitionStatus(companyId, vehicleId, request.status(), request.assignedPartnerId());
        return new ApiResponse<>(null);
    }
}
