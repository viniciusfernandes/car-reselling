package br.com.carreselling.usecase.partner.history.contract;

import java.util.List;

public record PartnerHistoryResponse(List<PartnerHistoryItem> history) {
}
