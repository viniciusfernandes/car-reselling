package br.com.carreselling.usecase.partner.disable.endpoint;

import br.com.carreselling.application.service.IPartnerService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@Validated
public class DeletePartnerEndpoint {

    private final IPartnerService partnerService;

    private final TenantContext tenantContext;

    public DeletePartnerEndpoint(IPartnerService partnerService, TenantContext tenantContext) {
        this.partnerService = partnerService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> disable(@PathVariable UUID id) {
        int companyId = tenantContext.getCurrentCompanyId();
        partnerService.disablePartner(companyId, id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}
