package br.com.carreselling.usecase.vehicle.taxes.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.application.service.model.VehicleTaxes;
import br.com.carreselling.config.ApiResponse;
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

    public VehicleTaxesEndpoint(IVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/{vehicleId}/taxes")
    public ApiResponse<VehicleTaxesResponse> getTaxes(@PathVariable UUID vehicleId) {
        VehicleTaxes taxes = vehicleService.getVehicleTaxes(vehicleId);
        return new ApiResponse<>(VehicleTaxesMapper.toResponse(taxes));
    }
}
