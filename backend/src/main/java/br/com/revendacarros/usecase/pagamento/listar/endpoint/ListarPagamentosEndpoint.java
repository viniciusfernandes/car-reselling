package br.com.revendacarros.usecase.pagamento.listar.endpoint;

import br.com.revendacarros.application.service.IPagamentoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.domain.model.TipoPagamento;
import br.com.revendacarros.usecase.pagamento.listar.contract.ListaPagamentosResponse;
import br.com.revendacarros.usecase.pagamento.listar.mapping.ListaPagamentosMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class ListarPagamentosEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public ListarPagamentosEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ListaPagamentosResponse>> list(
            @RequestParam(required = false) TipoPagamento tipoPagamento,
            @RequestParam(required = false) String mesPagamento,
            @RequestParam(required = false) String placa) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        String[] pagamentoDates = mesPagamento != null ? mesPagamento.split("-") : null;
        Integer ano = pagamentoDates != null ? Integer.parseInt(pagamentoDates[0]) : null;
        Integer mes = pagamentoDates != null ? Integer.parseInt(pagamentoDates[1]) : null;
        var pagamentos = pagamentoService.listarPagamentos(empresaId, tipoPagamento, ano, mes, placa)
                .stream()
                .map(ListaPagamentosMapper::toItem)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(new ListaPagamentosResponse(pagamentos)));
    }
}
