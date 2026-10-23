package br.com.revendacarros.usecase.pagamento.documento.listar.endpoint;

import br.com.revendacarros.application.service.IPagamentoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.pagamento.documento.listar.contract.DocumentoPagamentoItem;
import br.com.revendacarros.usecase.pagamento.documento.listar.contract.ListaDocumentosPagamentoResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class ListarDocumentosPagamentoEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public ListarDocumentosPagamentoEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{pagamentoId}/documentos")
    public ResponseEntity<ApiResponse<ListaDocumentosPagamentoResponse>> list(@PathVariable UUID pagamentoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<DocumentoPagamentoItem> documentos = pagamentoService.listarDocumentosPagamento(empresaId, pagamentoId)
            .stream()
            .map(doc -> new DocumentoPagamentoItem(
                doc.id(),
                doc.pagamentoId(),
                doc.nomeOriginalArquivo(),
                doc.contentType(),
                doc.tamanhoBytes(),
                doc.enviadoEm()
            ))
            .toList();
        return ResponseEntity.ok(new ApiResponse<>(new ListaDocumentosPagamentoResponse(documentos)));
    }
}
