package br.com.revendacarros.usecase.veiculo.distribuicao.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.veiculo.distribuicao.contract.AtribuirParceiroRequest;
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
public class AtribuirParceiroEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public AtribuirParceiroEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{veiculoId}/distribuicao")
    public ApiResponse<Void> assign(@PathVariable UUID veiculoId,
                                    @Valid @RequestBody AtribuirParceiroRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        veiculoService.atribuirParceiro(empresaId, veiculoId, request.parceiroId());
        return new ApiResponse<>(null);
    }
}
