package br.com.revendacarros.usecase.financeiro.saldocaixa.obter.endpoint;

import br.com.revendacarros.application.service.ISaldoCaixaService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.domain.model.SaldoCaixa;
import br.com.revendacarros.usecase.financeiro.saldocaixa.contract.SaldoCaixaResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financeiro")
@Validated
public class ObterSaldoCaixaEndpoint {

    private final ISaldoCaixaService saldoCaixaService;

    private final TenantContext tenantContext;

    public ObterSaldoCaixaEndpoint(ISaldoCaixaService saldoCaixaService, TenantContext tenantContext) {
        this.saldoCaixaService = saldoCaixaService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/saldo-caixa")
    public ApiResponse<SaldoCaixaResponse> get() {
        int empresaId = tenantContext.getCurrentEmpresaId();
        SaldoCaixa saldoCaixa = saldoCaixaService.getSaldoCaixa(empresaId);
        return new ApiResponse<>(new SaldoCaixaResponse(saldoCaixa.valor, saldoCaixa.atualizadoEm));
    }
}
