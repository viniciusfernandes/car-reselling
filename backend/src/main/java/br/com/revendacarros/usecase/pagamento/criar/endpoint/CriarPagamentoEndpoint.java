package br.com.revendacarros.usecase.pagamento.criar.endpoint;

import br.com.revendacarros.application.service.IPagamentoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.pagamento.criar.contract.CriarPagamentoRequest;
import br.com.revendacarros.usecase.pagamento.criar.contract.CriarPagamentoResponse;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagamentos")
@Validated
public class CriarPagamentoEndpoint {

    private final IPagamentoService pagamentoService;

    private final TenantContext tenantContext;

    public CriarPagamentoEndpoint(IPagamentoService pagamentoService, TenantContext tenantContext) {
        this.pagamentoService = pagamentoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CriarPagamentoResponse>> create(@Valid @RequestBody CriarPagamentoRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        UUID id = pagamentoService.criarPagamento(empresaId, 
                request.tipoPagamento(),
                request.descricao(),
                request.valor(),
                request.dataPagamento(),
                request.veiculoPlaca(),
                request.observacoes()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(new CriarPagamentoResponse(id)));
    }
}
