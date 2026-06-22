package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Partner;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartnerRepository {

    Partner savePartner(int companyId, Partner partner);

    Partner updatePartner(int companyId, Partner partner);

    /** Returns only partners with enabled = true. */
    List<Partner> findEnabledPartners(int companyId);

    Optional<Partner> findPartnerById(int companyId, UUID id);

    Optional<Partner> findPartnerByName(int companyId, String name);

    void setEnabled(int companyId, UUID id, boolean enabled);
}
