package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Partner;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartnerRepository {

    Partner savePartner(Partner partner);

    Partner updatePartner(Partner partner);

    /** Returns only partners with enabled = true. */
    List<Partner> findEnabledPartners();

    Optional<Partner> findPartnerById(UUID id);

    Optional<Partner> findPartnerByName(String name);

    void setEnabled(UUID id, boolean enabled);
}
