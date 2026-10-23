package br.com.revendacarros.usecase.veiculo.venda.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.veiculo.venda.contract.AtualizarPrecoVendaRequest;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class AtualizarPrecoVendaEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public AtualizarPrecoVendaEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @PutMapping("/{veiculoId}/preco-venda")
    public ApiResponse<Void> update(@PathVariable UUID veiculoId,
                                    @Valid @RequestBody AtualizarPrecoVendaRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        veiculoService.updatePrecoVenda(empresaId, veiculoId, request.precoVenda());
        return new ApiResponse<>(null);
    }
}
