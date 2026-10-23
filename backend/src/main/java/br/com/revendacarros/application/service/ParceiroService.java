package br.com.revendacarros.application.service;

import br.com.revendacarros.common.UuidGenerator;
import br.com.revendacarros.application.service.model.ResumoHistoricoParceiro;
import br.com.revendacarros.application.service.model.ResumoParceiro;
import br.com.revendacarros.domain.exception.ConflictException;
import br.com.revendacarros.domain.exception.NotFoundException;
import br.com.revendacarros.domain.model.Parceiro;
import br.com.revendacarros.domain.model.HistoricoParceiro;
import br.com.revendacarros.domain.repository.HistoricoParceiroRepository;
import br.com.revendacarros.domain.repository.ParceiroRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ParceiroService implements IParceiroService {

    private final ParceiroRepository parceiroRepository;
    private final HistoricoParceiroRepository historicoParceiroRepository;

    public ParceiroService(ParceiroRepository parceiroRepository,
                          HistoricoParceiroRepository historicoParceiroRepository) {
        this.parceiroRepository = parceiroRepository;
        this.historicoParceiroRepository = historicoParceiroRepository;
    }

    @Override
    public UUID criarParceiro(int empresaId, String nome, String cidade, String telefone, String email, BigDecimal taxaComissao) {
        parceiroRepository.findParceiroByNome(empresaId, nome)
            .ifPresent(existing -> {
                throw new ConflictException("Já existe um parceiro com esse nome");
            });
        Instant now = Instant.now();
        Parceiro parceiro = new Parceiro(
            UuidGenerator.generate(),
            empresaId,
            nome,
            cidade,
            telefone,
            email,
            taxaComissao,
            true,
            now,
            now
        );
        parceiroRepository.saveParceiro(empresaId, parceiro);
        historicoParceiroRepository.saveHistorico(empresaId, snapshotOf(empresaId, parceiro, "system"));
        return parceiro.getId();
    }

    @Override
    public List<ResumoParceiro> listarParceiros(int empresaId) {
        return parceiroRepository.findParceirosHabilitados(empresaId)
            .stream()
            .map(this::toResumo)
            .toList();
    }

    @Override
    public ResumoParceiro getParceiro(int empresaId, UUID id) {
        return parceiroRepository.findParceiroById(empresaId, id)
            .map(this::toResumo)
            .orElseThrow(() -> new NotFoundException("Parceiro não encontrado"));
    }

    @Override
    public void updateParceiro(int empresaId, UUID id, String nome, String cidade, String telefone, String email,
                              BigDecimal taxaComissao, String alteradoPor) {
        Parceiro existing = parceiroRepository.findParceiroById(empresaId, id)
            .orElseThrow(() -> new NotFoundException("Parceiro não encontrado"));

        parceiroRepository.findParceiroByNome(empresaId, nome)
            .filter(p -> !p.getId().equals(id))
            .ifPresent(p -> {
                throw new ConflictException("Já existe um parceiro com esse nome");
            });

        Parceiro updated = new Parceiro(
            existing.getId(),
            empresaId,
            nome,
            cidade,
            telefone,
            email,
            taxaComissao,
            existing.isHabilitado(),
            existing.getCriadoEm(),
            Instant.now()
        );
        parceiroRepository.updateParceiro(empresaId, updated);
        historicoParceiroRepository.saveHistorico(empresaId, snapshotOf(empresaId, updated, alteradoPor));
    }

    @Override
    public List<ResumoHistoricoParceiro> getHistoricoParceiro(int empresaId, UUID id) {
        parceiroRepository.findParceiroById(empresaId, id)
            .orElseThrow(() -> new NotFoundException("Parceiro não encontrado"));
        return historicoParceiroRepository.findHistoricoByParceiroId(empresaId, id)
            .stream()
            .map(h -> new ResumoHistoricoParceiro(
                h.getId(),
                h.getParceiroId(),
                h.getNome(),
                h.getCidade(),
                h.getTelefone(),
                h.getEmail(),
                h.getTaxaComissao(),
                h.getAlteradoEm(),
                h.getAlteradoPor()
            ))
            .toList();
    }

    private ResumoParceiro toResumo(Parceiro p) {
        return new ResumoParceiro(p.getId(), p.getNome(), p.getCidade(),
            p.getTelefone(), p.getEmail(), p.getTaxaComissao());
    }

    @Override
    public void desabilitarParceiro(int empresaId, UUID id) {
        Parceiro existing = parceiroRepository.findParceiroById(empresaId, id)
            .orElseThrow(() -> new NotFoundException("Parceiro não encontrado"));
        parceiroRepository.setHabilitado(empresaId, id, false);
        historicoParceiroRepository.saveHistorico(empresaId, snapshotOf(empresaId,
            new Parceiro(existing.getId(), empresaId, existing.getNome(), existing.getCidade(),
                existing.getTelefone(), existing.getEmail(), existing.getTaxaComissao(),
                false, existing.getCriadoEm(), Instant.now()),
            "system"
        ));
    }

    private HistoricoParceiro snapshotOf(int empresaId, Parceiro parceiro, String alteradoPor) {
        return new HistoricoParceiro(
            UuidGenerator.generate(),
            empresaId,
            parceiro.getId(),
            parceiro.getNome(),
            parceiro.getCidade(),
            parceiro.getTelefone(),
            parceiro.getEmail(),
            parceiro.getTaxaComissao(),
            Instant.now(),
            alteradoPor
        );
    }
}
