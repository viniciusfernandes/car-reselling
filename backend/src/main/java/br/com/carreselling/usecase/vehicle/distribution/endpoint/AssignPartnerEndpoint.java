package br.com.carreselling.usecase.vehicle.distribution.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.vehicle.distribution.contract.AssignPartnerRequest;
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
public class AssignPartnerEndpoint {

    private final IVehicleService vehicleService;

    private final TenantContext tenantContext;

    public AssignPartnerEndpoint(IVehicleService vehicleService, TenantContext tenantContext) {
        this.vehicleService = vehicleService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{vehicleId}/distribution")
    public ApiResponse<Void> assign(@PathVariable UUID vehicleId,
                                    @Valid @RequestBody AssignPartnerRequest request) {
        int companyId = tenantContext.getCurrentCompanyId();
        vehicleService.assignPartner(companyId, vehicleId, request.partnerId());
        return new ApiResponse<>(null);
    }
}
