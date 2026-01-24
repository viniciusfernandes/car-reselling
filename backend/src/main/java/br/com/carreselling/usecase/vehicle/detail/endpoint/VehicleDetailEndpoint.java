package br.com.carreselling.usecase.vehicle.detail.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.application.service.model.VehicleDetail;
import br.com.carreselling.config.ApiResponse;
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

    public VehicleDetailEndpoint(IVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/{vehicleId}")
    public ApiResponse<VehicleDetailResponse> detail(@PathVariable UUID vehicleId) {
        VehicleDetail detail = vehicleService.getVehicle(vehicleId);
        return new ApiResponse<>(VehicleDetailMapper.toResponse(detail));
    }
}
