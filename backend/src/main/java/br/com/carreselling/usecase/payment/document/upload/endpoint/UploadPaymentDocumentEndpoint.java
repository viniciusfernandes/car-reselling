package br.com.carreselling.usecase.payment.document.upload.endpoint;

import br.com.carreselling.application.service.IPaymentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.payment.document.upload.contract.UploadPaymentDocumentResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/payments")
public class UploadPaymentDocumentEndpoint {

    private final IPaymentService paymentService;

    private final TenantContext tenantContext;

    public UploadPaymentDocumentEndpoint(IPaymentService paymentService, TenantContext tenantContext) {
        this.paymentService = paymentService;
        this.tenantContext = tenantContext;
    }

    @PostMapping(value = "/{paymentId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadPaymentDocumentResponse>> upload(
            @PathVariable UUID paymentId,
            @RequestParam("file") MultipartFile file) {
        int companyId = tenantContext.getCurrentCompanyId();
        UUID documentId = paymentService.uploadPaymentDocument(companyId, paymentId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new UploadPaymentDocumentResponse(documentId)));
    }
}
