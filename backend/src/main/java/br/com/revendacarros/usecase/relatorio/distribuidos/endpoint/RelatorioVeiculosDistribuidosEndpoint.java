package br.com.revendacarros.usecase.relatorio.distribuidos.endpoint;

import br.com.revendacarros.application.service.IRelatorioService;
import br.com.revendacarros.application.service.model.FiltroVeiculosDistribuidos;
import br.com.revendacarros.application.service.model.RelatorioVeiculosDistribuidos;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relatorios")
@Validated
public class RelatorioVeiculosDistribuidosEndpoint {

    private final IRelatorioService relatorioService;

    private final TenantContext tenantContext;

    public RelatorioVeiculosDistribuidosEndpoint(IRelatorioService relatorioService, TenantContext tenantContext) {
        this.relatorioService = relatorioService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/veiculos-distribuidos")
    public ApiResponse<RelatorioVeiculosDistribuidos> relatorio(
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataFim,
        @RequestParam(required = false) String marca,
        @RequestParam(required = false) String modelo,
        @RequestParam(required = false) UUID parceiroId
    ) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        FiltroVeiculosDistribuidos filter = new FiltroVeiculosDistribuidos(
            dataInicio,
            dataFim,
            marca,
            modelo,
            parceiroId
        );
        return new ApiResponse<>(relatorioService.relatorioVeiculosDistribuidos(empresaId, filter));
    }
}
