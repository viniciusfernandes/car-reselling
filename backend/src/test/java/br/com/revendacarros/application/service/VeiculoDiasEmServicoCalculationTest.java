package br.com.revendacarros.application.service;

import br.com.revendacarros.domain.model.ServicoNoVeiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class VeiculoDiasEmServicoCalculationTest {
    @InjectMocks
    ServicoNoVeiculoService servicoNoVeiculoService;
    private final UUID servicoId = UUID.randomUUID();
    private final UUID veiculoId = UUID.randomUUID();
    private static final int EMPRESA_ID = 1;

    @Test
    void testDaysFromASingleIntervalOneDayAheadToday() {
        List<ServicoNoVeiculo> servicos = buildServicosOneDayAheadToday();
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,servicos)).isEqualByComparingTo(1L);
    }

    @Test
    void testDaysFromTwoDisjoinedIntervalsAndDaysAheadToday() {
        List<ServicoNoVeiculo> servicos = buildServicosDisjoinedWithAllAheadDates();
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,servicos)).isEqualByComparingTo(2L);
    }

    @Test
    void testDaysFromTwoJoinedIntervalsAndDaysAhead() {
        List<ServicoNoVeiculo> servicos = buildServicosOnVeiculoJoinedAheadDates();
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,servicos)).isEqualByComparingTo(2L);
    }

    @Test
    void testDaysFromFinishedService() {
        List<ServicoNoVeiculo> servicos = buildWithFinishedServicos();
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,servicos)).isEqualByComparingTo(2L);
    }

    @Test
    void testDaysFromFinishedServicoWithJoinedDates() {
        List<ServicoNoVeiculo> servicos = buildWithFinishedServicosWithJoinedDates();
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,servicos)).isEqualByComparingTo(15L);
    }

    @Test
    void testDaysFromServicoWithNoDataFim() {
        List<ServicoNoVeiculo> servicos = buildServicoWithNoDataFim();
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,servicos)).isEqualByComparingTo(10L);
    }

    @Test
    void testDaysFromTwoServicoWithNoDataFim() {
        List<ServicoNoVeiculo> servicos = buildTwoServicoWithNoDataFim();
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,servicos)).isEqualByComparingTo(25L);
    }

    @Test
    void testDaysFromTwoServicoWithOnlyOneWithNoDataFim() {
        List<ServicoNoVeiculo> servicos = buildTwoServicoWithOnlyOneWithNoDataFim();
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,servicos)).isEqualByComparingTo(30L);
    }

    @Test
    void testDaysFromNullLlist() {
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,(List<ServicoNoVeiculo>) null)).isEqualByComparingTo(0L);
    }

    @Test
    void testDaysFromEmptyList() {
        assertThat(servicoNoVeiculoService.calcularTotalDiasServico(EMPRESA_ID,List.of())).isEqualByComparingTo(0L);
    }

    private List<ServicoNoVeiculo> buildServicosOneDayAheadToday() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(1L);
        LocalDate end = now.plusDays(1L);
        ServicoNoVeiculo service = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start, end);
        return List.of(service);
    }

    private List<ServicoNoVeiculo> buildServicosDisjoinedWithAllAheadDates() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(2L);
        // ahead date
        LocalDate end = now.plusDays(1L);

        // both ahead date
        LocalDate start2 = now.plusDays(2L);
        LocalDate end2 = now.plusDays(3L);

        ServicoNoVeiculo service = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start, end);
        ServicoNoVeiculo servico2 = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start2, end2);
        return List.of(service, servico2);
    }

    private List<ServicoNoVeiculo> buildServicosOnVeiculoJoinedAheadDates() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(2L);
        // ahead date
        LocalDate end = now.plusDays(3L);

        // ahead dates
        LocalDate start2 = now.plusDays(2L);
        LocalDate end2 = now.plusDays(4L);

        ServicoNoVeiculo service = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start, end);
        ServicoNoVeiculo servico2 = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start2, end2);
        return List.of(service, servico2);
    }

    private List<ServicoNoVeiculo> buildWithFinishedServicos() {
        LocalDate now = LocalDate.now();
        // two day difference
        LocalDate start = now.minusDays(10L);
        LocalDate end = now.minusDays(8L);

        ServicoNoVeiculo service = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start, end);
        return List.of(service);
    }

    private List<ServicoNoVeiculo> buildWithFinishedServicosWithJoinedDates() {
        LocalDate now = LocalDate.now();
        // ten day difference
        LocalDate start = now.minusDays(30L);
        LocalDate end = now.minusDays(20L);

        //ten days difference
        LocalDate start2 = now.minusDays(25L);
        LocalDate end2 = now.minusDays(15L);

        ServicoNoVeiculo service = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start, end);
        ServicoNoVeiculo servico2 = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start2, end2);
        return List.of(service, servico2);
    }

    private List<ServicoNoVeiculo> buildServicoWithNoDataFim() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(10L);

        ServicoNoVeiculo service = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start, null);
        return List.of(service);
    }

    private List<ServicoNoVeiculo> buildTwoServicoWithNoDataFim() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(10L);
        LocalDate start2 = now.minusDays(25L);

        ServicoNoVeiculo service = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start, null);
        ServicoNoVeiculo servico2 = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start2, null);
        return List.of(service, servico2);
    }

    private List<ServicoNoVeiculo> buildTwoServicoWithOnlyOneWithNoDataFim() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(10L);
        LocalDate end = now.minusDays(8L);

        LocalDate start2 = now.minusDays(30L);

        ServicoNoVeiculo service = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start, end);
        ServicoNoVeiculo servico2 = new ServicoNoVeiculo(servicoId, EMPRESA_ID, veiculoId, start2, null);
        return List.of(service, servico2);
    }
}
