package br.com.carreselling.usecase.service.delete.endpoint;

import br.com.carreselling.application.service.IServiceEntryService;
import br.com.carreselling.config.ApiResponse;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class DeleteServiceEndpoint {

    private final IServiceEntryService serviceEntryService;

    public DeleteServiceEndpoint(IServiceEntryService serviceEntryService) {
        this.serviceEntryService = serviceEntryService;
    }

    @DeleteMapping("/{vehicleId}/services/{serviceId}")
    public ApiResponse<Void> delete(@PathVariable UUID vehicleId, @PathVariable UUID serviceId) {
        serviceEntryService.deleteService(vehicleId, serviceId);
        return new ApiResponse<>(null);
    }
}
