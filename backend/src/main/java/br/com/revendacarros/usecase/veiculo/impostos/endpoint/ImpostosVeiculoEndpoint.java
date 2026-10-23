package br.com.revendacarros.usecase.veiculo.impostos.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.application.service.model.ImpostosVeiculo;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.veiculo.impostos.contract.ImpostosVeiculoResponse;
import br.com.revendacarros.usecase.veiculo.impostos.mapping.ImpostosVeiculoMapper;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class ImpostosVeiculoEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public ImpostosVeiculoEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{veiculoId}/impostos")
    public ApiResponse<ImpostosVeiculoResponse> getImpostos(@PathVariable UUID veiculoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        ImpostosVeiculo impostos = veiculoService.getImpostosVeiculo(empresaId, veiculoId);
        return new ApiResponse<>(ImpostosVeiculoMapper.toResponse(impostos));
    }
}
