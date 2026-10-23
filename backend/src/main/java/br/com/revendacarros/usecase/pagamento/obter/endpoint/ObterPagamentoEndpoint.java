package br.com.revendacarros.usecase.pagamento.obter.endpoint;

import br.com.revendacarros.application.service.IPagamentoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.pagamento.listar.contract.PagamentoItem;
import br.com.revendacarros.usecase.pagamento.listar.mapping.ListaPagamentosMapper;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class ObterPagamentoEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public ObterPagamentoEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagamentoItem>> get(@PathVariable UUID id) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        PagamentoItem item = ListaPagamentosMapper.toItem(pagamentoService.getPagamento(empresaId, id));
        return ResponseEntity.ok(new ApiResponse<>(item));
    }
}
