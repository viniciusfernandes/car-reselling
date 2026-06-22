package br.com.carreselling.usecase.document.delete.endpoint;

import br.com.carreselling.application.service.IDocumentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class DeleteDocumentEndpoint {

    private final IDocumentService documentService;

    private final TenantContext tenantContext;

    public DeleteDocumentEndpoint(IDocumentService documentService, TenantContext tenantContext) {
        this.documentService = documentService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{vehicleId}/documents/{documentId}")
    public ApiResponse<Void> delete(@PathVariable UUID vehicleId, @PathVariable UUID documentId) {
        int companyId = tenantContext.getCurrentCompanyId();
        documentService.deleteDocument(companyId, vehicleId, documentId);
        return new ApiResponse<>(null);
    }
}
