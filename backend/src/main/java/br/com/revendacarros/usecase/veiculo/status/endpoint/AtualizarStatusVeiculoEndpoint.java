package br.com.revendacarros.usecase.veiculo.status.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.veiculo.status.contract.AtualizarStatusVeiculoRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class AtualizarStatusVeiculoEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public AtualizarStatusVeiculoEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{veiculoId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID veiculoId,
                                          @Valid @RequestBody AtualizarStatusVeiculoRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        veiculoService.transitionStatus(empresaId, veiculoId, request.status(), request.parceiroAtribuidoId());
        return new ApiResponse<>(null);
    }
}
