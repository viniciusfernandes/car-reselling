package br.com.revendacarros.usecase.parceiro.criar.endpoint;

import br.com.revendacarros.application.service.IParceiroService;
import br.com.revendacarros.config.ApiResponse;
import br.com.revendacarros.tenant.TenantContext;
import br.com.revendacarros.usecase.parceiro.criar.contract.CriarParceiroRequest;
import br.com.revendacarros.usecase.parceiro.criar.contract.CriarParceiroResponse;
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
@RequestMapping("/api/v1/parceiros")
@Validated
public class CriarParceiroEndpoint {

    private final IParceiroService parceiroService;

    private final TenantContext tenantContext;

    public CriarParceiroEndpoint(IParceiroService parceiroService, TenantContext tenantContext) {
        this.parceiroService = parceiroService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CriarParceiroResponse>> create(@Valid @RequestBody CriarParceiroRequest request) {
        int empresaId = tenantContext.getCurrentEmpresaId();
        UUID parceiroId = parceiroService.criarParceiro(empresaId, 
            request.nome(),
            request.cidade(),
            request.telefone(),
            request.email(),
            request.taxaComissao()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new CriarParceiroResponse(parceiroId)));
    }
}
