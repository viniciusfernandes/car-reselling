package br.com.carreselling.application.service;

import br.com.carreselling.common.UuidGenerator;
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
    public UUID createPartner(int companyId, String name, String city, String phone, String email, BigDecimal commissionRate) {
        partnerRepository.findPartnerByName(companyId, name)
            .ifPresent(existing -> {
                throw new ConflictException("Partner name already exists");
            });
        Instant now = Instant.now();
        Partner partner = new Partner(
            UuidGenerator.generate(),
            companyId,
            name,
            city,
            phone,
            email,
            commissionRate,
            true,
            now,
            now
        );
        partnerRepository.savePartner(companyId, partner);
        partnerHistoryRepository.saveHistory(companyId, snapshotOf(companyId, partner, "system"));
        return partner.getId();
    }

    @Override
    public List<PartnerSummary> listPartners(int companyId) {
        return partnerRepository.findEnabledPartners(companyId)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Override
    public PartnerSummary getPartner(int companyId, UUID id) {
        return partnerRepository.findPartnerById(companyId, id)
            .map(this::toSummary)
            .orElseThrow(() -> new NotFoundException("Partner not found"));
    }

    @Override
    public void updatePartner(int companyId, UUID id, String name, String city, String phone, String email,
                              BigDecimal commissionRate, String changedBy) {
        Partner existing = partnerRepository.findPartnerById(companyId, id)
            .orElseThrow(() -> new NotFoundException("Partner not found"));

        partnerRepository.findPartnerByName(companyId, name)
            .filter(p -> !p.getId().equals(id))
            .ifPresent(p -> {
                throw new ConflictException("Partner name already exists");
            });

        Partner updated = new Partner(
            existing.getId(),
            companyId,
            name,
            city,
            phone,
            email,
            commissionRate,
            existing.isEnabled(),
            existing.getCreatedAt(),
            Instant.now()
        );
        partnerRepository.updatePartner(companyId, updated);
        partnerHistoryRepository.saveHistory(companyId, snapshotOf(companyId, updated, changedBy));
    }

    @Override
    public List<PartnerHistorySummary> getPartnerHistory(int companyId, UUID id) {
        partnerRepository.findPartnerById(companyId, id)
            .orElseThrow(() -> new NotFoundException("Partner not found"));
        return partnerHistoryRepository.findHistoryByPartnerId(companyId, id)
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

    @Override
    public void disablePartner(int companyId, UUID id) {
        Partner existing = partnerRepository.findPartnerById(companyId, id)
            .orElseThrow(() -> new NotFoundException("Partner not found"));
        partnerRepository.setEnabled(companyId, id, false);
        partnerHistoryRepository.saveHistory(companyId, snapshotOf(companyId,
            new Partner(existing.getId(), companyId, existing.getName(), existing.getCity(),
                existing.getPhone(), existing.getEmail(), existing.getCommissionRate(),
                false, existing.getCreatedAt(), Instant.now()),
            "system"
        ));
    }

    private PartnerHistory snapshotOf(int companyId, Partner partner, String changedBy) {
        return new PartnerHistory(
            UuidGenerator.generate(),
            companyId,
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
