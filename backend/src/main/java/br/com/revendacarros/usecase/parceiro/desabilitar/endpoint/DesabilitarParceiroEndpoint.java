package br.com.revendacarros.usecase.parceiro.desabilitar.endpoint;

import br.com.revendacarros.application.service.IParceiroService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parceiros")
@Validated
public class DesabilitarParceiroEndpoint {

    private final IParceiroService parceiroService;

    private final TenantContext tenantContext;

    public DesabilitarParceiroEndpoint(IParceiroService parceiroService, TenantContext tenantContext) {
        this.parceiroService = parceiroService;
        this.tenantContext = tenantContext;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> disable(@PathVariable UUID id) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        parceiroService.desabilitarParceiro(empresaId, id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}
