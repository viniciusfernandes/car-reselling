package br.com.carreselling.usecase.partner.detail.endpoint;

import br.com.carreselling.application.service.IPartnerService;
import br.com.carreselling.application.service.model.PartnerSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.partner.detail.contract.PartnerDetailResponse;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partners")
@Validated
public class GetPartnerEndpoint {

    private final IPartnerService partnerService;

    public GetPartnerEndpoint(IPartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping("/{id}")
    public ApiResponse<PartnerDetailResponse> get(@PathVariable UUID id) {
        PartnerSummary partner = partnerService.getPartner(id);
        return new ApiResponse<>(new PartnerDetailResponse(
            partner.id(),
            partner.name(),
            partner.city(),
            partner.phone(),
            partner.email(),
            partner.commissionRate()
        ));
    }
}
