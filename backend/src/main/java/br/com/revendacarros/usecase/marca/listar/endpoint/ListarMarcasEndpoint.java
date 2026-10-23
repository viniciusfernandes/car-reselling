package br.com.revendacarros.usecase.marca.listar.endpoint;

import br.com.revendacarros.application.service.IMarcaService;
import br.com.revendacarros.application.service.model.ResumoMarca;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.marca.listar.contract.MarcaItem;
import br.com.revendacarros.usecase.marca.listar.contract.ListaMarcasResponse;
import br.com.revendacarros.usecase.marca.listar.mapping.ListaMarcasMapper;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/marcas")
@Validated
public class ListarMarcasEndpoint {

    private final IMarcaService marcaService;

    private final TenantContext tenantContext;

    public ListarMarcasEndpoint(IMarcaService marcaService, TenantContext tenantContext) {
        this.marcaService = marcaService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ApiResponse<ListaMarcasResponse> list() {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<ResumoMarca> marcas = marcaService.listarMarcas(empresaId);
        List<MarcaItem> items = marcas.stream()
            .map(ListaMarcasMapper::toItem)
            .toList();
        return new ApiResponse<>(new ListaMarcasResponse(items));
    }
}
