package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoServico;
import br.com.revendacarros.common.UuidGenerator;
import br.com.revendacarros.domain.exception.NotFoundException;
import br.com.revendacarros.domain.model.ServicoNoVeiculo;
import br.com.revendacarros.domain.model.TipoServico;
import br.com.revendacarros.domain.model.Veiculo;
import br.com.revendacarros.domain.repository.ServicoRepository;
import br.com.revendacarros.domain.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

@Service
public class ServicoNoVeiculoService implements IServicoNoVeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;

    public ServicoNoVeiculoService(VeiculoRepository veiculoRepository, ServicoRepository servicoRepository) {
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
    }

    @Override
    public UUID addService(int empresaId,
                           UUID veiculoId,
                           TipoServico tipoServico,
                           BigDecimal valorServico,
                           String descricao,
                           LocalDate dataInicio,
                           LocalDate dataFim) {
        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        veiculo.ensureServicosEditable();
        if (valorServico == null || valorServico.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("valorServico: não pode ser negativo.");
        }
        if (dataInicio == null) {
            throw new IllegalArgumentException("dataInicio: obrigatório.");
        }
        ServicoNoVeiculo entry = new ServicoNoVeiculo(
                UuidGenerator.generate(),
                empresaId,
                veiculoId,
                tipoServico,
                descricao,
                valorServico,
                dataInicio,
                dataFim,
                Instant.now(),
                Instant.now()
        );
        servicoRepository.saveService(empresaId, entry);
        return entry.getId();
    }

    @Override
    public List<ResumoServico> listarServicos(int empresaId, UUID veiculoId) {
        veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        return servicoRepository.findServicoByVeiculoId(empresaId, veiculoId)
                .stream()
                .map(service -> new ResumoServico(
                        service.getId(),
                        service.getVeiculoId(),
                        service.getTipoServico(),
                        service.getDescricao(),
                        service.getValorServico(),
                        service.getDataInicio(),
                        service.getDataFim()
                ))
                .toList();
    }

    @Override
    public BigDecimal totalServicos(int empresaId, UUID veiculoId) {
        return servicoRepository.findServicoTotalByVeiculoId(empresaId, veiculoId);
    }

    @Override
    public void updateService(int empresaId,
                              UUID veiculoId,
                              UUID servicoId,
                              TipoServico tipoServico,
                              BigDecimal valorServico,
                              String descricao,
                              LocalDate dataInicio,
                              LocalDate dataFim) {
        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        veiculo.ensureServicosEditable();
        if (dataInicio == null) {
            throw new IllegalArgumentException("dataInicio: obrigatório.");
        }
        ServicoNoVeiculo service = servicoRepository.findServicoById(empresaId, servicoId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado"));
        if (!service.getVeiculoId().equals(veiculoId)) {
            throw new NotFoundException("Serviço não encontrado para o veículo");
        }
        service.update(tipoServico, descricao, valorServico, dataInicio, dataFim);
        service.setAtualizadoEm(Instant.now());
        servicoRepository.updateService(empresaId, service);
    }

    @Override
    public void deleteService(int empresaId, UUID veiculoId, UUID servicoId) {
        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        veiculo.ensureServicosEditable();
        ServicoNoVeiculo service = servicoRepository.findServicoById(empresaId, servicoId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado"));
        if (!service.getVeiculoId().equals(veiculoId)) {
            throw new NotFoundException("Serviço não encontrado para o veículo");
        }
        servicoRepository.deleteService(empresaId, servicoId);
    }

    @Override
    public long calcularTotalDiasServico(int empresaId, List<ServicoNoVeiculo> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            return 0L;
        }
        List<ServicoNoVeiculo> orderedServicos = servicos.stream()
                .sorted((s1, s2) -> s1.getDataInicio().compareTo(s2.getDataInicio()))
                .toList();

        ServicoNoVeiculo first = orderedServicos.getFirst();
        LocalDate now = LocalDate.now();
        if (!first.getDataInicio().isBefore(now)) {
            return 0;
        }

        Stack<LocalDate> dates = new Stack<>();
        LocalDate start = first.getDataInicio();
        LocalDate end = first.getDataFim() == null || first.getDataFim().isAfter(now) ? now : first.getDataFim();

        if (servicos.size() == 1) {
            return daysBetween(start, end);
        }

        dates.add(start);
        dates.add(end);
        for (int j = 1; j < orderedServicos.size(); j++) {
            LocalDate last = dates.pop();
            if (!last.isBefore(now)) {
                dates.add(now);
                break;
            }
            ServicoNoVeiculo next = orderedServicos.get(j);
            if (last.isBefore(next.getDataInicio())) {
                dates.add(last);
                dates.add(next.getDataInicio());
                if (next.getDataFim() != null) {
                    dates.add(next.getDataFim());
                }
            } else if (next.getDataFim() != null && next.getDataFim() != null && last.isBefore(next.getDataFim())) {
                dates.add(next.getDataFim());
            } else if (next.getDataFim() != null && !last.isBefore(next.getDataFim())) {
                dates.add(last);
            } else if (next.getDataFim() == null) {
                dates.add(now);
            }
            j++;
        }
        return totalDays(dates);
    }

    @Override
    public double calcularTotalDiasServico(int empresaId, UUID veiculoId) {
        List<ServicoNoVeiculo> servicos = servicoRepository.findServicoByVeiculoId(empresaId, veiculoId);
        return calcularTotalDiasServico(empresaId, servicos);
    }

    private long totalDays(Stack<LocalDate> dates) {
        int j = 0;
        long totalDays = 0;
        while (j < dates.size()) {
            totalDays += daysBetween(dates.get(j), dates.get(j + 1));
            j = j + 2;
        }
        return totalDays;
    }

    private long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

}
