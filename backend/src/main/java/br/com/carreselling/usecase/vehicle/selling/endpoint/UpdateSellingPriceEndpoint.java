package br.com.carreselling.usecase.vehicle.selling.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.vehicle.selling.contract.UpdateSellingPriceRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class UpdateSellingPriceEndpoint {

    private final IVehicleService vehicleService;

    public UpdateSellingPriceEndpoint(IVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PutMapping("/{vehicleId}/selling-price")
    public ApiResponse<Void> update(@PathVariable UUID vehicleId,
                                    @Valid @RequestBody UpdateSellingPriceRequest request) {
        vehicleService.updateSellingPrice(vehicleId, request.sellingPrice());
        return new ApiResponse<>(null);
    }
}
