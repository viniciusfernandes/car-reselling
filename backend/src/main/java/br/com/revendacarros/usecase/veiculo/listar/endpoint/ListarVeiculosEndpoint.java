package br.com.revendacarros.usecase.veiculo.listar.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.application.service.model.ResumoVeiculo;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.domain.model.StatusVeiculo;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.veiculo.listar.contract.ItemListaVeiculo;
import br.com.revendacarros.usecase.veiculo.listar.contract.ListaVeiculosResponse;
import br.com.revendacarros.usecase.veiculo.listar.mapping.ListaVeiculosMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class ListarVeiculosEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public ListarVeiculosEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ApiResponse<ListaVeiculosResponse> list(@RequestParam(required = false) StatusVeiculo status,
                                                 @RequestParam(required = false) String q,
                                                 @RequestParam(required = false) Boolean emServico,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<ResumoVeiculo> veiculos = veiculoService.listarVeiculos(empresaId, status, q, emServico, page, size);
        long total = veiculoService.countVeiculos(empresaId, status, q, emServico);
        List<ItemListaVeiculo> items = veiculos.stream()
                .map(ListaVeiculosMapper::toItem)
                .toList();
        return new ApiResponse<>(new ListaVeiculosResponse(items, page, size, total));
    }
}
