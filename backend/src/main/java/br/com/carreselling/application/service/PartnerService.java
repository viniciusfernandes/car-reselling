package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.PartnerSummary;
import br.com.carreselling.domain.exception.ConflictException;
import br.com.carreselling.domain.model.Partner;
import br.com.carreselling.domain.repository.PartnerRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PartnerService implements IPartnerService {

    private final PartnerRepository partnerRepository;

    public PartnerService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @Override
    public UUID createPartner(String name, String city) {
        partnerRepository.findPartnerByName(name)
            .ifPresent(existing -> {
                throw new ConflictException("Partner name already exists");
            });
        Partner partner = new Partner(
            UUID.randomUUID(),
            name,
            city,
            null,
            Instant.now(),
            Instant.now()
        );
        partnerRepository.savePartner(partner);
        return partner.getId();
    }

    @Override
    public List<PartnerSummary> listPartners() {
        return partnerRepository.findPartner()
            .stream()
            .map(partner -> new PartnerSummary(
                partner.getId(),
                partner.getName(),
                partner.getCity()
            ))
            .toList();
    }
}
