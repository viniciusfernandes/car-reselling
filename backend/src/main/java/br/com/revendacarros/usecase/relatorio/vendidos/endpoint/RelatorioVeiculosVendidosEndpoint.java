package br.com.revendacarros.usecase.relatorio.vendidos.endpoint;

import br.com.revendacarros.application.service.IRelatorioService;
import br.com.revendacarros.application.service.model.FiltroVeiculosDistribuidos;
import br.com.revendacarros.application.service.model.RelatorioVeiculosVendidos;
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
public class RelatorioVeiculosVendidosEndpoint {

    private final IRelatorioService relatorioService;

    private final TenantContext tenantContext;

    public RelatorioVeiculosVendidosEndpoint(IRelatorioService relatorioService, TenantContext tenantContext) {
        this.relatorioService = relatorioService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/veiculos-vendidos")
    public ApiResponse<RelatorioVeiculosVendidos> relatorio(
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
        return new ApiResponse<>(relatorioService.relatorioVeiculosVendidos(empresaId, filter));
    }
}
