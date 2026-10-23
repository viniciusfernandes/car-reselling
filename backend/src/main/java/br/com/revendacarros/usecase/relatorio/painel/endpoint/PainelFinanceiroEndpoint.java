package br.com.revendacarros.usecase.relatorio.painel.endpoint;

import br.com.revendacarros.application.service.IPainelService;
import br.com.revendacarros.application.service.model.PainelFinanceiro;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/relatorios")
@Validated
public class PainelFinanceiroEndpoint {

    private final IPainelService painelService;

    private final TenantContext tenantContext;

    public PainelFinanceiroEndpoint(IPainelService painelService, TenantContext tenantContext) {
        this.painelService = painelService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/painel-financeiro")
    public ApiResponse<PainelFinanceiro> getPainelFinanceiro(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        int empresaId = tenantContext.getCurrentEmpresaId();

        return new ApiResponse<>(painelService.getPainelFinanceiro(empresaId, dataInicio, dataFim));
    }
}
