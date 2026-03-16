package br.com.carreselling.usecase.vehicle.delete.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.config.ApiResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
public class DeleteVehicleEndpoint {

    private final IVehicleService vehicleService;

    public DeleteVehicleEndpoint(IVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
    }
}
