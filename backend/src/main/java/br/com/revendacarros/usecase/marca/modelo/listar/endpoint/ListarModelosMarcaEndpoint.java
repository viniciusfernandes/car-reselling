package br.com.revendacarros.usecase.marca.modelo.listar.endpoint;

import br.com.revendacarros.application.service.IModeloVeiculoService;
import br.com.revendacarros.application.service.model.ResumoModeloVeiculo;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.marca.modelo.listar.contract.ModeloVeiculoItem;
import br.com.revendacarros.usecase.marca.modelo.listar.contract.ListaModelosVeiculoResponse;
import br.com.revendacarros.usecase.marca.modelo.listar.mapping.ListaModelosVeiculoMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/marcas/{marcaId}/modelos")
@Validated
public class ListarModelosMarcaEndpoint {

    private final IModeloVeiculoService modeloVeiculoService;

    private final TenantContext tenantContext;

    public ListarModelosMarcaEndpoint(IModeloVeiculoService modeloVeiculoService, TenantContext tenantContext) {
        this.modeloVeiculoService = modeloVeiculoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ApiResponse<ListaModelosVeiculoResponse> list(@PathVariable UUID marcaId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<ResumoModeloVeiculo> modelos = modeloVeiculoService.listarModelosByMarcaId(empresaId, marcaId);
        List<ModeloVeiculoItem> items = modelos.stream()
            .map(ListaModelosVeiculoMapper::toItem)
            .toList();
        return new ApiResponse<>(new ListaModelosVeiculoResponse(items));
    }
}
