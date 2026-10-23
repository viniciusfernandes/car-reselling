package br.com.revendacarros.usecase.servico.excluir.endpoint;

import br.com.revendacarros.application.service.IServicoNoVeiculoService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/veiculos")
@Validated
public class ExcluirServicoEndpoint {

    private final IServicoNoVeiculoService servicoNoVeiculoService;

    private final TenantContext tenantContext;

    public ExcluirServicoEndpoint(IServicoNoVeiculoService servicoNoVeiculoService, TenantContext tenantContext) {
        this.servicoNoVeiculoService = servicoNoVeiculoService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{veiculoId}/servicos/{servicoId}")
    public ApiResponse<Void> delete(@PathVariable UUID veiculoId, @PathVariable UUID servicoId) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        servicoNoVeiculoService.deleteService(empresaId, veiculoId, servicoId);
        return new ApiResponse<>(null);
    }
}
