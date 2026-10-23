package br.com.revendacarros.usecase.pagamento.documento.upload.endpoint;

import br.com.revendacarros.application.service.IPagamentoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.pagamento.documento.upload.contract.UploadDocumentoPagamentoResponse;
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
@RequestMapping("/api/v1/pagamentos")
public class UploadDocumentoPagamentoEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public UploadDocumentoPagamentoEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping(value = "/{pagamentoId}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadDocumentoPagamentoResponse>> upload(
            @PathVariable UUID pagamentoId,
            @RequestParam("file") MultipartFile file) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        UUID documentoId = pagamentoService.uploadDocumentoPagamento(empresaId, pagamentoId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new UploadDocumentoPagamentoResponse(documentoId)));
    }
}
