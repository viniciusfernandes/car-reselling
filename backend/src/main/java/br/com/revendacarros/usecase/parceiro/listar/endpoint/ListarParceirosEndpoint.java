package br.com.revendacarros.usecase.parceiro.listar.endpoint;

import br.com.revendacarros.application.service.IParceiroService;
import br.com.revendacarros.application.service.model.ResumoParceiro;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.parceiro.listar.contract.ParceiroItem;
import br.com.revendacarros.usecase.parceiro.listar.contract.ListaParceirosResponse;
import br.com.revendacarros.usecase.parceiro.listar.mapping.ListaParceirosMapper;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parceiros")
@Validated
public class ListarParceirosEndpoint {

    private final IParceiroService parceiroService;

    private final TenantContext tenantContext;

    public ListarParceirosEndpoint(IParceiroService parceiroService, TenantContext tenantContext) {
        this.parceiroService = parceiroService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ApiResponse<ListaParceirosResponse> list() {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<ResumoParceiro> parceiros = parceiroService.listarParceiros(empresaId);
        List<ParceiroItem> items = parceiros.stream()
                .map(ListaParceirosMapper::toItem)
                .toList();
        return new ApiResponse<>(new ListaParceirosResponse(items));
    }
}
