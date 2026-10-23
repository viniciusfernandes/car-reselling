package br.com.revendacarros.usecase.financeiro.saldocaixa.atualizar.endpoint;

import br.com.revendacarros.application.service.ISaldoCaixaService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.UseCaseException;
import br.com.revendacarros.usecase.financeiro.saldocaixa.atualizar.contract.AtualizarSaldoCaixaRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financeiro")
@Validated
public class AtualizarSaldoCaixaEndpoint {

    private final ISaldoCaixaService saldoCaixaService;

    private final TenantContext tenantContext;

    public AtualizarSaldoCaixaEndpoint(ISaldoCaixaService saldoCaixaService, TenantContext tenantContext) {
        this.saldoCaixaService = saldoCaixaService;
        this.tenantContext = tenantContext;
    }

    @PutMapping("/saldo-caixa")
    public ApiResponse<Void> update(
            @Valid @RequestBody AtualizarSaldoCaixaRequest request,
            Authentication authentication
    ) throws UseCaseException {
        int empresaId = tenantContext.getCurrentEmpresaId();
        String alteradoPor = authentication != null ? authentication.getName() : null;
        saldoCaixaService.updateSaldoCaixa(empresaId, request.valor(), alteradoPor);
        return new ApiResponse<>(null);
    }
}
