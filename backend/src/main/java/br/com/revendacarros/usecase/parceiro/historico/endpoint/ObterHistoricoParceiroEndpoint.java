package br.com.revendacarros.usecase.parceiro.historico.endpoint;

import br.com.revendacarros.application.service.IParceiroService;
import br.com.revendacarros.application.service.model.ResumoHistoricoParceiro;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.parceiro.historico.contract.HistoricoParceiroItem;
import br.com.revendacarros.usecase.parceiro.historico.contract.HistoricoParceiroResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parceiros")
@Validated
public class ObterHistoricoParceiroEndpoint {

    private final IParceiroService parceiroService;

    private final TenantContext tenantContext;

    public ObterHistoricoParceiroEndpoint(IParceiroService parceiroService, TenantContext tenantContext) {
        this.parceiroService = parceiroService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/historico")
    public ApiResponse<HistoricoParceiroResponse> historico(@PathVariable UUID id) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<ResumoHistoricoParceiro> summaries = parceiroService.getHistoricoParceiro(empresaId, id);
        List<HistoricoParceiroItem> items = summaries.stream()
            .map(s -> new HistoricoParceiroItem(
                s.id(),
                s.nome(),
                s.cidade(),
                s.telefone(),
                s.email(),
                s.taxaComissao(),
                s.alteradoEm(),
                s.alteradoPor()
            ))
            .toList();
        return new ApiResponse<>(new HistoricoParceiroResponse(items));
    }
}
