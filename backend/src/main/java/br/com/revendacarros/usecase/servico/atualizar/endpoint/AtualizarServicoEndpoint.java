package br.com.revendacarros.usecase.servico.atualizar.endpoint;

import br.com.revendacarros.application.service.IServicoNoVeiculoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.servico.atualizar.contract.AtualizarServicoRequest;
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
public class AtualizarServicoEndpoint {

    private final IServicoNoVeiculoService servicoNoVeiculoService;

    private final TenantContext tenantContext;

    public AtualizarServicoEndpoint(IServicoNoVeiculoService servicoNoVeiculoService, TenantContext tenantContext) {
        this.servicoNoVeiculoService = servicoNoVeiculoService;
        this.tenantContext = tenantContext;
    }

    @PutMapping("/{veiculoId}/servicos/{servicoId}")
    public ApiResponse<Void> update(@PathVariable UUID veiculoId,
                                    @PathVariable UUID servicoId,
                                    @Valid @RequestBody AtualizarServicoRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        servicoNoVeiculoService.updateService(empresaId, 
            veiculoId,
            servicoId,
            request.tipoServico(),
            request.valorServico(),
            request.descricao(),
            request.dataInicio(),
            request.dataFim()
        );
        return new ApiResponse<>(null);
    }
}
