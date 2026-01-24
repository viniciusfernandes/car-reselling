package br.com.carreselling.usecase.partner.list.endpoint;

import br.com.carreselling.application.service.IPartnerService;
import br.com.carreselling.application.service.model.PartnerSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.partner.list.contract.PartnerItem;
import br.com.carreselling.usecase.partner.list.contract.PartnerListResponse;
import br.com.carreselling.usecase.partner.list.mapping.PartnerListMapper;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partners")
@Validated
public class ListPartnerEndpoint {

    private final IPartnerService partnerService;

    public ListPartnerEndpoint(IPartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping
    public ApiResponse<PartnerListResponse> list() {
        List<PartnerSummary> partners = partnerService.listPartners();
        List<PartnerItem> items = partners.stream()
            .map(PartnerListMapper::toItem)
            .toList();
        return new ApiResponse<>(new PartnerListResponse(items));
    }
}
