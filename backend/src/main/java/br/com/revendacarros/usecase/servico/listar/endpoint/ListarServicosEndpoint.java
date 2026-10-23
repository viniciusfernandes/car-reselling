package br.com.revendacarros.usecase.servico.listar.endpoint;

import br.com.revendacarros.application.service.IServicoNoVeiculoService;
import br.com.revendacarros.application.service.model.ResumoServico;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.servico.listar.contract.ServicoItem;
import br.com.revendacarros.usecase.servico.listar.contract.ListaServicosResponse;
import br.com.revendacarros.usecase.servico.listar.mapping.ListaServicosMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class ListarServicosEndpoint {

    private final IServicoNoVeiculoService servicoNoVeiculoService;

    private final TenantContext tenantContext;

    public ListarServicosEndpoint(IServicoNoVeiculoService servicoNoVeiculoService, TenantContext tenantContext) {
        this.servicoNoVeiculoService = servicoNoVeiculoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{veiculoId}/servicos")
    public ApiResponse<ListaServicosResponse> list(@PathVariable UUID veiculoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        List<ResumoServico> servicos = servicoNoVeiculoService.listarServicos(empresaId, veiculoId);
        List<ServicoItem> items = servicos.stream()
            .map(ListaServicosMapper::toItem)
            .toList();
        return new ApiResponse<>(new ListaServicosResponse(items, servicoNoVeiculoService.totalServicos(empresaId, veiculoId)));
    }
}
