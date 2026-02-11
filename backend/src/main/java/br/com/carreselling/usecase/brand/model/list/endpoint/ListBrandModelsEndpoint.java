package br.com.carreselling.usecase.brand.model.list.endpoint;

import br.com.carreselling.application.service.IVehicleModelService;
import br.com.carreselling.application.service.model.VehicleModelSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.brand.model.list.contract.VehicleModelItem;
import br.com.carreselling.usecase.brand.model.list.contract.VehicleModelListResponse;
import br.com.carreselling.usecase.brand.model.list.mapping.VehicleModelListMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands/{brandId}/models")
@Validated
public class ListBrandModelsEndpoint {

    private final IVehicleModelService vehicleModelService;

    public ListBrandModelsEndpoint(IVehicleModelService vehicleModelService) {
        this.vehicleModelService = vehicleModelService;
    }

    @GetMapping
    public ApiResponse<VehicleModelListResponse> list(@PathVariable UUID brandId) {
        List<VehicleModelSummary> models = vehicleModelService.listModelsByBrandId(brandId);
        List<VehicleModelItem> items = models.stream()
            .map(VehicleModelListMapper::toItem)
            .toList();
        return new ApiResponse<>(new VehicleModelListResponse(items));
    }
}
