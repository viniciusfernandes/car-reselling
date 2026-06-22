package br.com.carreselling.usecase.vehicle.delete.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
public class DeleteVehicleEndpoint {

    private final IVehicleService vehicleService;

    private final TenantContext tenantContext;

    public DeleteVehicleEndpoint(IVehicleService vehicleService, TenantContext tenantContext) {
        this.vehicleService = vehicleService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        int companyId = tenantContext.getCurrentCompanyId();
        vehicleService.deleteVehicle(companyId, id);
    }
}
