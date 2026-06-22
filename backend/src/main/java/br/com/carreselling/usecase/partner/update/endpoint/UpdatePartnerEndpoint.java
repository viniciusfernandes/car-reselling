package br.com.carreselling.usecase.partner.update.endpoint;

import br.com.carreselling.application.service.IPartnerService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.partner.update.contract.UpdatePartnerRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partners")
@Validated
public class UpdatePartnerEndpoint {

    private final IPartnerService partnerService;

    private final TenantContext tenantContext;

    public UpdatePartnerEndpoint(IPartnerService partnerService, TenantContext tenantContext) {
        this.partnerService = partnerService;
        this.tenantContext = tenantContext;
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable UUID id,
                                    @Valid @RequestBody UpdatePartnerRequest request,
                                    Authentication authentication) {
        int companyId = tenantContext.getCurrentCompanyId();
        String changedBy = authentication != null ? authentication.getName() : "unknown";
        partnerService.updatePartner(companyId, 
            id,
            request.name(),
            request.city(),
            request.phone(),
            request.email(),
            request.commissionRate(),
            changedBy
        );
        return new ApiResponse<>(null);
    }
}
