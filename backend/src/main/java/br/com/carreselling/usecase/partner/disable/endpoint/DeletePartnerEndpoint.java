package br.com.carreselling.usecase.partner.disable.endpoint;

import br.com.carreselling.application.service.IPartnerService;
import br.com.carreselling.config.ApiResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@Validated
public class DeletePartnerEndpoint {

    private final IPartnerService partnerService;

    public DeletePartnerEndpoint(IPartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> disable(@PathVariable UUID id) {
        partnerService.disablePartner(id);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}
