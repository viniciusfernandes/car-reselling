package br.com.carreselling.usecase.payment.document.download.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.carreselling.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/payments")
public class DownloadPaymentDocumentEndpoint {

    private final IPaymentService paymentService;

    private final TenantContext tenantContext;

    public DownloadPaymentDocumentEndpoint(IPaymentService paymentService, TenantContext tenantContext) {
        this.paymentService = paymentService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{paymentId}/documents/{documentId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID paymentId,
                                             @PathVariable UUID documentId) {
        int companyId = tenantContext.getCurrentCompanyId();
        Resource resource = paymentService.downloadPaymentDocument(companyId, paymentId, documentId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
}
