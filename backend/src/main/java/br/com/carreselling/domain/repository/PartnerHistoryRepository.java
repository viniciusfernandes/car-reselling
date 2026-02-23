package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.PartnerHistory;
import java.util.List;
import java.util.UUID;

public interface PartnerHistoryRepository {

    void saveHistory(PartnerHistory history);

    List<PartnerHistory> findHistoryByPartnerId(UUID partnerId);
}
