package br.com.carreselling.usecase.vehicle.create.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.config.ApiResponse;
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

    public CreateVehicleEndpoint(IVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateVehicleResponse>> create(@Valid @RequestBody CreateVehicleRequest request) {
        CreateVehicleRequest normalized = CreateVehicleMapper.normalize(request);
        UUID vehicleId = vehicleService.createVehicle(
            normalized.licensePlate(),
            normalized.renavam(),
            normalized.vin(),
            normalized.year(),
            normalized.color(),
            normalized.model(),
            normalized.brand(),
            normalized.supplierSource(),
            normalized.purchasePrice(),
            normalized.freightCost()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new CreateVehicleResponse(vehicleId)));
    }
}
