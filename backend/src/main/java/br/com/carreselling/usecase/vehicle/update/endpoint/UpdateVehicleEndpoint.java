package br.com.carreselling.usecase.vehicle.update.endpoint;

import br.com.carreselling.application.service.IVehicleService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.vehicle.update.contract.UpdateVehicleRequest;
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
public class UpdateVehicleEndpoint {

    private final IVehicleService vehicleService;

    public UpdateVehicleEndpoint(IVehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PutMapping("/{vehicleId}")
    public ApiResponse<Void> update(@PathVariable UUID vehicleId, @Valid @RequestBody UpdateVehicleRequest request) {
        vehicleService.updateVehicle(
            vehicleId,
            request.year(),
            request.color(),
            request.model(),
            request.brand(),
            request.supplierSource(),
            request.purchasePrice(),
            request.freightCost(),
            request.purchaseInvoiceDocumentId(),
            request.purchasePaymentReceiptDocumentId()
        );
        return new ApiResponse<>(null);
    }
}
