package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Partner;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartnerRepository {

    Partner savePartner(Partner partner);

    List<Partner> findPartner();

    Optional<Partner> findPartnerById(UUID id);

    Optional<Partner> findPartnerByName(String name);
}
