package br.com.revendacarros.usecase.pagamento.documento.excluir.endpoint;

import br.com.revendacarros.application.service.IPagamentoService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.revendacarros.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class ExcluirDocumentoPagamentoEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public ExcluirDocumentoPagamentoEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{pagamentoId}/documentos/{documentoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID pagamentoId,
                                       @PathVariable UUID documentoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        pagamentoService.deleteDocumentoPagamento(empresaId, pagamentoId, documentoId);
        return ResponseEntity.noContent().build();
    }
}
