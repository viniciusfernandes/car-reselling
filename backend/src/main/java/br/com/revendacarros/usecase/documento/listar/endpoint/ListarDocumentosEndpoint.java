package br.com.revendacarros.usecase.documento.listar.endpoint;

import br.com.revendacarros.application.service.IDocumentoService;
import br.com.revendacarros.application.service.model.ResumoDocumento;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.documento.listar.contract.DocumentoItem;
import br.com.revendacarros.usecase.documento.listar.contract.ListaDocumentosResponse;
import br.com.revendacarros.usecase.documento.listar.mapping.ListaDocumentosMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class ListarDocumentosEndpoint {

    private final IDocumentoService documentoService;

    private final TenantContext tenantContext;

    public ListarDocumentosEndpoint(IDocumentoService documentoService, TenantContext tenantContext) {
        this.documentoService = documentoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{veiculoId}/documentos")
    public ApiResponse<ListaDocumentosResponse> list(@PathVariable UUID veiculoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<ResumoDocumento> documentos = documentoService.listarDocumentos(empresaId, veiculoId);
        List<DocumentoItem> items = documentos.stream()
            .map(ListaDocumentosMapper::toItem)
            .toList();
        return new ApiResponse<>(new ListaDocumentosResponse(items));
    }
}
