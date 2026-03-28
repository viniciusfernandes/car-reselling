package br.com.carreselling.usecase.vehicle.onservice.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.config.ApiResponse;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
public class ToggleOnServiceEndpoint {

    private final IVehicleService vehicleService;

    public ToggleOnServiceEndpoint(IVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/{vehicleId}/on-service/toggle")
    public ApiResponse<Map<String, Boolean>> toggle(@PathVariable UUID vehicleId) {
        boolean newValue = vehicleService.toggleOnService(vehicleId);
        return new ApiResponse<>(Map.of("onService", newValue));
    }
}
