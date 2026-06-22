package br.com.carreselling.usecase.partner.create.endpoint;

import br.com.carreselling.application.service.IPartnerService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.partner.create.contract.CreatePartnerRequest;
import br.com.carreselling.usecase.partner.create.contract.CreatePartnerResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partners")
@Validated
public class CreatePartnerEndpoint {

    private final IPartnerService partnerService;

    private final TenantContext tenantContext;

    public CreatePartnerEndpoint(IPartnerService partnerService, TenantContext tenantContext) {
        this.partnerService = partnerService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePartnerResponse>> create(@Valid @RequestBody CreatePartnerRequest request) {
        int companyId = tenantContext.getCurrentCompanyId();
        UUID partnerId = partnerService.createPartner(companyId, 
            request.name(),
            request.city(),
            request.phone(),
            request.email(),
            request.commissionRate()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new CreatePartnerResponse(partnerId)));
    }
}
