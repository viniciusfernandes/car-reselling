package br.com.revendacarros.usecase.servico.adicionar.endpoint;

import br.com.revendacarros.application.service.IServicoNoVeiculoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.servico.adicionar.contract.AdicionarServicoRequest;
import br.com.revendacarros.usecase.servico.adicionar.contract.AdicionarServicoResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class AdicionarServicoEndpoint {

    private final IServicoNoVeiculoService servicoNoVeiculoService;

    private final TenantContext tenantContext;

    public AdicionarServicoEndpoint(IServicoNoVeiculoService servicoNoVeiculoService, TenantContext tenantContext) {
        this.servicoNoVeiculoService = servicoNoVeiculoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{veiculoId}/servicos")
    public ResponseEntity<ApiResponse<AdicionarServicoResponse>> add(@PathVariable UUID veiculoId,
                                                               @Valid @RequestBody AdicionarServicoRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        UUID servicoId = servicoNoVeiculoService.addService(empresaId, 
            veiculoId,
            request.tipoServico(),
            request.valorServico(),
            request.descricao(),
            request.dataInicio(),
            request.dataFim()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new AdicionarServicoResponse(servicoId)));
    }
}
