package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.PartnerHistorySummary;
import br.com.carreselling.application.service.model.PartnerSummary;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IPartnerService {

    UUID createPartner(int companyId, String name, String city, String phone, String email, BigDecimal commissionRate);

    List<PartnerSummary> listPartners(int companyId);

    PartnerSummary getPartner(int companyId, UUID id);

    void updatePartner(int companyId, UUID id, String name, String city, String phone, String email,
                       BigDecimal commissionRate, String changedBy);

    List<PartnerHistorySummary> getPartnerHistory(int companyId, UUID id);

    void disablePartner(int companyId, UUID id);
}
