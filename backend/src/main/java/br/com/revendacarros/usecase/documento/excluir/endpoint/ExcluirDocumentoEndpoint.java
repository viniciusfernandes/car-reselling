package br.com.revendacarros.usecase.documento.excluir.endpoint;

import br.com.revendacarros.application.service.IDocumentoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class ExcluirDocumentoEndpoint {

    private final IDocumentoService documentoService;

    private final TenantContext tenantContext;

    public ExcluirDocumentoEndpoint(IDocumentoService documentoService, TenantContext tenantContext) {
        this.documentoService = documentoService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{veiculoId}/documentos/{documentoId}")
    public ApiResponse<Void> delete(@PathVariable UUID veiculoId, @PathVariable UUID documentoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        documentoService.deleteDocumento(empresaId, veiculoId, documentoId);
        return new ApiResponse<>(null);
    }
}
