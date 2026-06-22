package br.com.carreselling.usecase.service.add.endpoint;

import br.com.carreselling.application.service.IServiceEntryService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.service.add.contract.AddServiceRequest;
import br.com.carreselling.usecase.service.add.contract.AddServiceResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class AddServiceEndpoint {

    private final IServiceEntryService serviceEntryService;

    private final TenantContext tenantContext;

    public AddServiceEndpoint(IServiceEntryService serviceEntryService, TenantContext tenantContext) {
        this.serviceEntryService = serviceEntryService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{vehicleId}/services")
    public ResponseEntity<ApiResponse<AddServiceResponse>> add(@PathVariable UUID vehicleId,
                                                               @Valid @RequestBody AddServiceRequest request) {
        int companyId = tenantContext.getCurrentCompanyId();
        UUID serviceId = serviceEntryService.addService(companyId, 
            vehicleId,
            request.serviceType(),
            request.serviceValue(),
            request.description(),
            request.startDate(),
            request.endDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new AddServiceResponse(serviceId)));
    }
}
