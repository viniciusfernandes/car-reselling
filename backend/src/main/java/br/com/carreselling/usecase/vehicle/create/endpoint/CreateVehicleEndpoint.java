package br.com.carreselling.usecase.vehicle.create.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.vehicle.create.contract.CreateVehicleRequest;
import br.com.carreselling.usecase.vehicle.create.contract.CreateVehicleResponse;
import br.com.carreselling.usecase.vehicle.create.mapping.CreateVehicleMapper;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class CreateVehicleEndpoint {

    private final IVehicleService vehicleService;

    private final TenantContext tenantContext;

    public CreateVehicleEndpoint(IVehicleService vehicleService, TenantContext tenantContext) {
        this.vehicleService = vehicleService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateVehicleResponse>> create(@Valid @RequestBody CreateVehicleRequest request) {
        int companyId = tenantContext.getCurrentCompanyId();
        CreateVehicleRequest normalized = CreateVehicleMapper.normalize(request);
        UUID vehicleId = vehicleService.createVehicle(companyId, 
            normalized.licensePlate(),
            normalized.renavam(),
            normalized.vin(),
            normalized.year(),
            normalized.color(),
            normalized.model(),
            normalized.brand(),
            normalized.supplierSource(),
            normalized.purchasePrice(),
            normalized.freightCost(),
            normalized.purchaseCommission(),
            normalized.valorFipe()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new CreateVehicleResponse(vehicleId)));
    }
}
