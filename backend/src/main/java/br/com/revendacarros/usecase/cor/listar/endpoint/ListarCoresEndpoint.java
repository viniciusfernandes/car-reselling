package br.com.revendacarros.usecase.cor.listar.endpoint;

import br.com.revendacarros.application.service.ICorService;
import br.com.revendacarros.application.service.model.ResumoCor;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.cor.listar.contract.CorItem;
import br.com.revendacarros.usecase.cor.listar.contract.ListaCoresResponse;
import br.com.revendacarros.usecase.cor.listar.mapping.ListaCoresMapper;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cores")
@Validated
public class ListarCoresEndpoint {

    private final ICorService corService;

    private final TenantContext tenantContext;

    public ListarCoresEndpoint(ICorService corService, TenantContext tenantContext) {
        this.corService = corService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ApiResponse<ListaCoresResponse> list() {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<ResumoCor> cores = corService.listarCores(empresaId);
        List<CorItem> items = cores.stream()
            .map(ListaCoresMapper::toItem)
            .toList();
        return new ApiResponse<>(new ListaCoresResponse(items));
    }
}
