package br.com.revendacarros.usecase.documento.upload.endpoint;

import br.com.revendacarros.application.service.IDocumentoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.domain.model.TipoDocumento;
import br.com.revendacarros.usecase.documento.upload.contract.UploadDocumentoResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class UploadDocumentoEndpoint {

    private final IDocumentoService documentoService;

    private final TenantContext tenantContext;

    public UploadDocumentoEndpoint(IDocumentoService documentoService, TenantContext tenantContext) {
        this.documentoService = documentoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping(value = "/{veiculoId}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadDocumentoResponse>> upload(@PathVariable UUID veiculoId,
                                                                      @RequestParam TipoDocumento tipoDocumento,
                                                                      @RequestParam("file") MultipartFile file) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        UUID documentoId = documentoService.uploadDocumento(empresaId, veiculoId, tipoDocumento, file);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new UploadDocumentoResponse(documentoId)));
    }
}
