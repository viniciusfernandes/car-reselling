package br.com.revendacarros.usecase.veiculo.criar.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.veiculo.criar.contract.CriarVeiculoRequest;
import br.com.revendacarros.usecase.veiculo.criar.contract.CriarVeiculoResponse;
import br.com.revendacarros.usecase.veiculo.criar.mapping.CriarVeiculoMapper;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class CriarVeiculoEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public CriarVeiculoEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CriarVeiculoResponse>> create(@Valid @RequestBody CriarVeiculoRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        CriarVeiculoRequest normalized = CriarVeiculoMapper.normalize(request);
        UUID veiculoId = veiculoService.criarVeiculo(empresaId, 
            normalized.placa(),
            normalized.renavam(),
            normalized.chassi(),
            normalized.ano(),
            normalized.cor(),
            normalized.modelo(),
            normalized.marca(),
            normalized.origemFornecedor(),
            normalized.precoCompra(),
            normalized.custoFrete(),
            normalized.comissaoCompra(),
            normalized.valorFipe()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new CriarVeiculoResponse(veiculoId)));
    }
}
