package br.com.revendacarros.usecase.parceiro.atualizar.endpoint;

import br.com.revendacarros.application.service.IParceiroService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.parceiro.atualizar.contract.AtualizarParceiroRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parceiros")
@Validated
public class AtualizarParceiroEndpoint {

    private final IParceiroService parceiroService;

    private final TenantContext tenantContext;

    public AtualizarParceiroEndpoint(IParceiroService parceiroService, TenantContext tenantContext) {
        this.parceiroService = parceiroService;
        this.tenantContext = tenantContext;
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable UUID id,
                                    @Valid @RequestBody AtualizarParceiroRequest request,
                                    Authentication authentication) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        String alteradoPor = authentication != null ? authentication.getName() : "unknown";
        parceiroService.updateParceiro(empresaId, 
            id,
            request.nome(),
            request.cidade(),
            request.telefone(),
            request.email(),
            request.taxaComissao(),
            alteradoPor
        );
        return new ApiResponse<>(null);
    }
}
