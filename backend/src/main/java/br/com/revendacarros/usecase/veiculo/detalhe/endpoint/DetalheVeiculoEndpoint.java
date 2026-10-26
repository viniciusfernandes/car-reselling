package br.com.revendacarros.usecase.veiculo.detalhe.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.application.service.model.DetalheVeiculo;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.veiculo.detalhe.contract.DetalheVeiculoResponse;
import br.com.revendacarros.usecase.veiculo.detalhe.mapping.DetalheVeiculoMapper;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class DetalheVeiculoEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public DetalheVeiculoEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{veiculoId}")
    public ApiResponse<DetalheVeiculoResponse> detail(@PathVariable UUID veiculoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        DetalheVeiculo detail = veiculoService.findVeiculo(empresaId, veiculoId);
        return new ApiResponse<>(DetalheVeiculoMapper.toResponse(detail));
    }
}
