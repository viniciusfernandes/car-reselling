package br.com.revendacarros.usecase.pagamento.descricoes.endpoint;

import br.com.revendacarros.application.service.IPagamentoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.domain.model.TipoPagamento;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class ListarDescricoesPagamentoEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public ListarDescricoesPagamentoEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/descricoes")
    public ResponseEntity<ApiResponse<List<String>>> list(
            @RequestParam(required = false) TipoPagamento tipoPagamento) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<String> descricoes = pagamentoService.listarDescricoes(empresaId, tipoPagamento);
        return ResponseEntity.ok(new ApiResponse<>(descricoes));
    }
}
