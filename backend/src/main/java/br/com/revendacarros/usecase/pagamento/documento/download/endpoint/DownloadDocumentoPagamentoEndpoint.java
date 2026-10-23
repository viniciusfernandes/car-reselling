package br.com.revendacarros.usecase.pagamento.documento.download.endpoint;

import br.com.revendacarros.application.service.IPagamentoService;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.revendacarros.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class DownloadDocumentoPagamentoEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public DownloadDocumentoPagamentoEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{pagamentoId}/documentos/{documentoId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID pagamentoId,
                                             @PathVariable UUID documentoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        Resource resource = pagamentoService.downloadDocumentoPagamento(empresaId, pagamentoId, documentoId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
}
