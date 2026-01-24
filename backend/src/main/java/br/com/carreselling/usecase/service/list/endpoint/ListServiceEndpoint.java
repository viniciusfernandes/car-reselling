package br.com.carreselling.usecase.service.list.endpoint;

import br.com.carreselling.application.service.IServiceEntryService;
import br.com.carreselling.application.service.model.ServiceSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.service.list.contract.ServiceItem;
import br.com.carreselling.usecase.service.list.contract.ServiceListResponse;
import br.com.carreselling.usecase.service.list.mapping.ServiceListMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class ListServiceEndpoint {

    private final IServiceEntryService serviceEntryService;

    public ListServiceEndpoint(IServiceEntryService serviceEntryService) {
        this.serviceEntryService = serviceEntryService;
    }

    @GetMapping("/{vehicleId}/services")
    public ApiResponse<ServiceListResponse> list(@PathVariable UUID vehicleId) {
        List<ServiceSummary> services = serviceEntryService.listServices(vehicleId);
        List<ServiceItem> items = services.stream()
            .map(ServiceListMapper::toItem)
            .toList();
        return new ApiResponse<>(new ServiceListResponse(items, serviceEntryService.totalServices(vehicleId)));
    }
}
