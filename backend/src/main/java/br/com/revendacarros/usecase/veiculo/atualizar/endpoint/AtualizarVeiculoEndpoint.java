package br.com.revendacarros.usecase.veiculo.atualizar.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.veiculo.atualizar.contract.AtualizarVeiculoRequest;
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
public class AtualizarVeiculoEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public AtualizarVeiculoEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @PutMapping("/{veiculoId}")
    public ApiResponse<Void> update(@PathVariable UUID veiculoId, @Valid @RequestBody AtualizarVeiculoRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        veiculoService.updateVeiculo(empresaId, 
            veiculoId,
            request.ano(),
            request.cor(),
            request.modelo(),
            request.marca(),
            request.origemFornecedor(),
            request.precoCompra(),
            request.custoFrete(),
            request.comissaoCompra(),
            request.documentoNotaFiscalCompraId(),
            request.documentoComprovantePagamentoCompraId(),
            request.valorFipe()
        );
        return new ApiResponse<>(null);
    }
}
