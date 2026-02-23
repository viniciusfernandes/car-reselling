package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.PartnerHistorySummary;
import br.com.carreselling.application.service.model.PartnerSummary;
import br.com.carreselling.domain.exception.ConflictException;
import br.com.carreselling.domain.exception.NotFoundException;
import br.com.carreselling.domain.model.Partner;
import br.com.carreselling.domain.model.PartnerHistory;
import br.com.carreselling.domain.repository.PartnerHistoryRepository;
import br.com.carreselling.domain.repository.PartnerRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PartnerService implements IPartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerHistoryRepository partnerHistoryRepository;

    public PartnerService(PartnerRepository partnerRepository,
                          PartnerHistoryRepository partnerHistoryRepository) {
        this.partnerRepository = partnerRepository;
        this.partnerHistoryRepository = partnerHistoryRepository;
    }

    @Override
    public UUID createPartner(String name, String city) {
        partnerRepository.findPartnerByName(name)
            .ifPresent(existing -> {
                throw new ConflictException("Partner name already exists");
            });
        Instant now = Instant.now();
        Partner partner = new Partner(
            UUID.randomUUID(),
            name,
            city,
            null,
            null,
            null,
            now,
            now
        );
        partnerRepository.savePartner(partner);
        partnerHistoryRepository.saveHistory(snapshotOf(partner, "system"));
        return partner.getId();
    }

    @Override
    public List<PartnerSummary> listPartners() {
        return partnerRepository.findPartner()
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Override
    public PartnerSummary getPartner(UUID id) {
        return partnerRepository.findPartnerById(id)
            .map(this::toSummary)
            .orElseThrow(() -> new NotFoundException("Partner not found"));
    }

    @Override
    public void updatePartner(UUID id, String name, String city, String phone, String email,
                              BigDecimal commissionRate, String changedBy) {
        Partner existing = partnerRepository.findPartnerById(id)
            .orElseThrow(() -> new NotFoundException("Partner not found"));

        partnerRepository.findPartnerByName(name)
            .filter(p -> !p.getId().equals(id))
            .ifPresent(p -> {
                throw new ConflictException("Partner name already exists");
            });

        Partner updated = new Partner(
            existing.getId(),
            name,
            city,
            phone,
            email,
            commissionRate,
            existing.getCreatedAt(),
            Instant.now()
        );
        partnerRepository.updatePartner(updated);
        partnerHistoryRepository.saveHistory(snapshotOf(updated, changedBy));
    }

    @Override
    public List<PartnerHistorySummary> getPartnerHistory(UUID id) {
        partnerRepository.findPartnerById(id)
            .orElseThrow(() -> new NotFoundException("Partner not found"));
        return partnerHistoryRepository.findHistoryByPartnerId(id)
            .stream()
            .map(h -> new PartnerHistorySummary(
                h.getId(),
                h.getPartnerId(),
                h.getName(),
                h.getCity(),
                h.getPhone(),
                h.getEmail(),
                h.getCommissionRate(),
                h.getChangedAt(),
                h.getChangedBy()
            ))
            .toList();
    }

    private PartnerSummary toSummary(Partner p) {
        return new PartnerSummary(p.getId(), p.getName(), p.getCity(),
            p.getPhone(), p.getEmail(), p.getCommissionRate());
    }

    private PartnerHistory snapshotOf(Partner partner, String changedBy) {
        return new PartnerHistory(
            UUID.randomUUID(),
            partner.getId(),
            partner.getName(),
            partner.getCity(),
            partner.getPhone(),
            partner.getEmail(),
            partner.getCommissionRate(),
            Instant.now(),
            changedBy
        );
    }
}
