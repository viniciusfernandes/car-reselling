package br.com.revendacarros.usecase.veiculo.excluir.endpoint;

import br.com.revendacarros.application.service.IVeiculoService;
import br.com.revendacarros.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/veiculos")
public class ExcluirVeiculoEndpoint {

    private final IVeiculoService veiculoService;

    private final TenantContext tenantContext;

    public ExcluirVeiculoEndpoint(IVeiculoService veiculoService, TenantContext tenantContext) {
        this.veiculoService = veiculoService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        veiculoService.deleteVeiculo(empresaId, id);
    }
}
