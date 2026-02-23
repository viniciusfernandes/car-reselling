package br.com.carreselling.usecase.partner.history.endpoint;

import br.com.carreselling.application.service.IPartnerService;
import br.com.carreselling.application.service.model.PartnerHistorySummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.partner.history.contract.PartnerHistoryItem;
import br.com.carreselling.usecase.partner.history.contract.PartnerHistoryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partners")
@Validated
public class GetPartnerHistoryEndpoint {

    private final IPartnerService partnerService;

    public GetPartnerHistoryEndpoint(IPartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping("/{id}/history")
    public ApiResponse<PartnerHistoryResponse> history(@PathVariable UUID id) {
        List<PartnerHistorySummary> summaries = partnerService.getPartnerHistory(id);
        List<PartnerHistoryItem> items = summaries.stream()
            .map(s -> new PartnerHistoryItem(
                s.id(),
                s.name(),
                s.city(),
                s.phone(),
                s.email(),
                s.commissionRate(),
                s.changedAt(),
                s.changedBy()
            ))
            .toList();
        return new ApiResponse<>(new PartnerHistoryResponse(items));
    }
}
