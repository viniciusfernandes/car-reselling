package br.com.carreselling.usecase.vehicle.list.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.application.service.model.VehicleSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.domain.model.VehicleStatus;
import br.com.carreselling.usecase.vehicle.list.contract.VehicleListItem;
import br.com.carreselling.usecase.vehicle.list.contract.VehicleListResponse;
import br.com.carreselling.usecase.vehicle.list.mapping.VehicleListMapper;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class ListVehicleEndpoint {

    private final IVehicleService vehicleService;

    public ListVehicleEndpoint(IVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ApiResponse<VehicleListResponse> list(@RequestParam(required = false) VehicleStatus status,
                                                 @RequestParam(required = false) String q,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        List<VehicleSummary> vehicles = vehicleService.listVehicles(status, q, page, size);
        long total = vehicleService.countVehicles(status, q);
        List<VehicleListItem> items = vehicles.stream()
            .map(VehicleListMapper::toItem)
            .toList();
        return new ApiResponse<>(new VehicleListResponse(items, page, size, total));
    }
}
