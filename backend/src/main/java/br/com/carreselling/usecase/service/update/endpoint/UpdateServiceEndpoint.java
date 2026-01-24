package br.com.carreselling.usecase.service.update.endpoint;

import br.com.carreselling.application.service.IServiceEntryService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.service.update.contract.UpdateServiceRequest;
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
public class UpdateServiceEndpoint {

    private final IServiceEntryService serviceEntryService;

    public UpdateServiceEndpoint(IServiceEntryService serviceEntryService) {
        this.serviceEntryService = serviceEntryService;
    }

    @PutMapping("/{vehicleId}/services/{serviceId}")
    public ApiResponse<Void> update(@PathVariable UUID vehicleId,
                                    @PathVariable UUID serviceId,
                                    @Valid @RequestBody UpdateServiceRequest request) {
        serviceEntryService.updateService(
            vehicleId,
            serviceId,
            request.serviceType(),
            request.serviceValue(),
            request.description(),
            request.performedAt()
        );
        return new ApiResponse<>(null);
    }
}
