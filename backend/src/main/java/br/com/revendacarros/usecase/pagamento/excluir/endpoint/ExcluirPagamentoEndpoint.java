package br.com.revendacarros.usecase.pagamento.excluir.endpoint;

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
public class ExcluirPagamentoEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public ExcluirPagamentoEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        pagamentoService.deletePagamento(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
