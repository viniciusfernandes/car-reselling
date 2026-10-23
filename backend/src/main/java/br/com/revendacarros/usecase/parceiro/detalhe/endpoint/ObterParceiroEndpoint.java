package br.com.revendacarros.usecase.parceiro.detalhe.endpoint;

import br.com.revendacarros.application.service.IParceiroService;
import br.com.revendacarros.application.service.model.ResumoParceiro;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.parceiro.detalhe.contract.DetalheParceiroResponse;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parceiros")
@Validated
public class ObterParceiroEndpoint {

    private final IParceiroService parceiroService;

    private final TenantContext tenantContext;

    public ObterParceiroEndpoint(IParceiroService parceiroService, TenantContext tenantContext) {
        this.parceiroService = parceiroService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public ApiResponse<DetalheParceiroResponse> get(@PathVariable UUID id) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        ResumoParceiro parceiro = parceiroService.getParceiro(empresaId, id);
        return new ApiResponse<>(new DetalheParceiroResponse(
            parceiro.id(),
            parceiro.nome(),
            parceiro.cidade(),
            parceiro.telefone(),
            parceiro.email(),
            parceiro.taxaComissao()
        ));
    }
}
