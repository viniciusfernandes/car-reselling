package br.com.revendacarros.usecase.documento.download.endpoint;

import br.com.revendacarros.application.service.IDocumentoService;
import br.com.revendacarros.application.service.model.ResumoDocumento;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.revendacarros.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class DownloadDocumentoEndpoint {

    private final IDocumentoService documentoService;

    private final TenantContext tenantContext;

    public DownloadDocumentoEndpoint(IDocumentoService documentoService, TenantContext tenantContext) {
        this.documentoService = documentoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{veiculoId}/documentos/{documentoId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID veiculoId, @PathVariable UUID documentoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        ResumoDocumento resumo = documentoService.getDocumento(empresaId, veiculoId, documentoId);
        Resource resource = documentoService.downloadDocumento(empresaId, veiculoId, documentoId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(resumo.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resumo.nomeOriginalArquivo() + "\"")
            .body(resource);
    }
}
