package br.com.revendacarros.application.service;

import br.com.revendacarros.common.UuidGenerator;
import br.com.revendacarros.application.service.model.DetalheVeiculo;
import br.com.revendacarros.application.service.model.ResumoVeiculo;
import br.com.revendacarros.application.service.model.ImpostosVeiculo;
import br.com.revendacarros.domain.exception.ConflictException;
import br.com.revendacarros.domain.exception.InvalidStateException;
import br.com.revendacarros.domain.exception.NotFoundException;
import br.com.revendacarros.domain.model.Marca;
import br.com.revendacarros.domain.model.Cor;
import br.com.revendacarros.domain.model.Parceiro;
import br.com.revendacarros.domain.model.OrigemFornecedor;
import br.com.revendacarros.domain.model.Veiculo;
import br.com.revendacarros.domain.model.ModeloVeiculo;
import br.com.revendacarros.domain.model.StatusVeiculo;
import br.com.revendacarros.domain.repository.*;
import br.com.revendacarros.infrastructure.storage.DocumentoStorage;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class VeiculoService implements IVeiculoService {
    private final VeiculoRepository veiculoRepository;
    private final HistoricoVeiculoEmServicoRepository historicoVeiculoEmServicoRepository;
    private final DocumentoRepository documentoRepository;
    private final DocumentoStorage documentoStorage;
    private final ServicoRepository servicoRepository;
    private final ParceiroRepository parceiroRepository;
    private final MarcaRepository marcaRepository;
    private final CorRepository corRepository;
    private final ModeloVeiculoRepository modeloVeiculoRepository;
    private final CalculadoraVendasVeiculo vendasCalculator;
    private final ServicoNoVeiculoService servicoNoVeiculoService;
    private final IPagamentoService pagamentoService;

    public VeiculoService(VeiculoRepository veiculoRepository, HistoricoVeiculoEmServicoRepository historicoVeiculoEmServicoRepository,
                          DocumentoRepository documentoRepository,
                          DocumentoStorage documentoStorage,
                          ServicoRepository servicoRepository,
                          ParceiroRepository parceiroRepository,
                          MarcaRepository marcaRepository,
                          CorRepository corRepository,
                          ModeloVeiculoRepository modeloVeiculoRepository,
                          CalculadoraVendasVeiculo vendasCalculator,
                          ServicoNoVeiculoService servicoNoVeiculoService,
                          IPagamentoService pagamentoService) {
        this.veiculoRepository = veiculoRepository;
        this.historicoVeiculoEmServicoRepository = historicoVeiculoEmServicoRepository;
        this.documentoRepository = documentoRepository;
        this.documentoStorage = documentoStorage;
        this.servicoRepository = servicoRepository;
        this.parceiroRepository = parceiroRepository;
        this.marcaRepository = marcaRepository;
        this.corRepository = corRepository;
        this.modeloVeiculoRepository = modeloVeiculoRepository;
        this.vendasCalculator = vendasCalculator;
        this.servicoNoVeiculoService = servicoNoVeiculoService;
        this.pagamentoService = pagamentoService;
    }

    @Override
    public UUID criarVeiculo(Veiculo veiculo) {
        veiculo.normalizar();
        int empresaId = veiculo.getEmpresaId();
       veiculo.validar();

        Instant now = Instant.now();
        Optional<Veiculo> existingByPlate = veiculoRepository.findVeiculoByPlaca(empresaId, veiculo.getPlaca());
        UUID currentVeiculoId = existingByPlate.map(Veiculo::getId).orElse(null);
        validarRenavamAndChassi(empresaId, veiculo.getRenavam(), veiculo.getChassi(), currentVeiculoId);
        resolveCor(empresaId, veiculo.getCor(), now);
        Marca marcaEntity = resolveMarca(empresaId, veiculo.getMarca(), now);
        ModeloVeiculo modeloEntity = resolveModelo(empresaId, marcaEntity.getId(), veiculo.getModelo(), now);
        if (existingByPlate.isPresent()) {
            Veiculo existingVeiculo = existingByPlate.get();
            existingVeiculo.setRenavam(veiculo.getRenavam());
            existingVeiculo.setChassi(veiculo.getChassi());
            existingVeiculo.updateDetails(
                    veiculo.getAno(),
                    veiculo.getCor(),
                    veiculo.getModelo(),
                    veiculo.getMarca(),
                    veiculo.getOrigemFornecedor(),
                    veiculo.getPrecoCompra(),
                    veiculo.getCustoFrete(),
                    veiculo.getComissaoCompra()
            );
            existingVeiculo.setMarcaId(marcaEntity.getId());
            existingVeiculo.setModeloId(modeloEntity.getId());
            existingVeiculo.setValorFipe(veiculo.getValorFipe());
            existingVeiculo.setAtualizadoEm(now);
            existingVeiculo.ensureDistribuicaoInvariant();
            veiculoRepository.updateVeiculo(empresaId, existingVeiculo);
            return existingVeiculo.getId();
        }
        veiculo.setMarcaId(marcaEntity.getId());
        veiculo.setModeloId(modeloEntity.getId());
        veiculo.setCriadoEm(now);
        veiculo.setAtualizadoEm(now);
        veiculo.ensureDistribuicaoInvariant();
        veiculoRepository.saveVeiculo(empresaId, veiculo);
        return veiculo.getId();
    }

    private void validarRenavamAndChassi(int empresaId, String renavam, String chassi, UUID currentVeiculoId) {
        if (StringUtils.hasText(renavam)) {
            veiculoRepository.findVeiculoByRenavam(empresaId, renavam)
                    .ifPresent(existing -> {
                        if (currentVeiculoId == null || !existing.getId().equals(currentVeiculoId)) {
                            throw new ConflictException("Renavam já cadastrado");
                        }
                    });
        }
        if (StringUtils.hasText(chassi)) {
            veiculoRepository.findVeiculoByChassi(empresaId, chassi)
                    .ifPresent(existing -> {
                        if (currentVeiculoId == null || !existing.getId().equals(currentVeiculoId)) {
                            throw new ConflictException("Chassi já cadastrado");
                        }
                    });
        }
    }

    @Override
    public DetalheVeiculo findVeiculo(int empresaId, UUID veiculoId) {
        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        BigDecimal totalServicos = veiculoRepository.findVeiculoTotalServicosByVeiculoId(empresaId, veiculoId);
        int documentosCount = veiculoRepository.countVeiculoDocumentosByVeiculoId(empresaId, veiculoId);
        BigDecimal custoTotal = veiculo.getPrecoCompra()
                .add(veiculo.getCustoFrete())
                .add(totalServicos);
        String nomeParceiro = resolveNomeParceiro(empresaId, veiculo.getParceiroAtribuidoId());
        BigDecimal comissaoCompra = veiculo.getComissaoCompra() == null
                ? BigDecimal.ZERO
                : veiculo.getComissaoCompra();
        boolean emServico = servicoRepository.existsOpenServicoByVeiculoId(empresaId, veiculoId);
        return new DetalheVeiculo(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getRenavam(),
                veiculo.getChassi(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getModelo(),
                veiculo.getMarca(),
                veiculo.getOrigemFornecedor(),
                veiculo.getPrecoCompra(),
                veiculo.getCustoFrete(),
                comissaoCompra,
                veiculo.getPrecoVenda(),
                veiculo.getValorFipe(),
                veiculo.getDocumentoNotaFiscalCompraId(),
                veiculo.getDocumentoComprovantePagamentoCompraId(),
                veiculo.getStatus(),
                emServico,
                veiculo.getParceiroAtribuidoId(),
                nomeParceiro,
                totalServicos,
                custoTotal,
                documentosCount,
                veiculo.getCriadoEm(),
                veiculo.getAtualizadoEm(),
                veiculo.getDistribuidoEm()
        );
    }

    @Override
    public void updatePrecoVenda(int empresaId, UUID veiculoId, BigDecimal precoVenda) {
        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));

        veiculo.transitionStatus(StatusVeiculo.VENDIDO);
        veiculo.updatePrecoVenda(precoVenda);
        stampDataVenda(veiculo);
        stampTaxaComissao(empresaId, veiculo);
        veiculo.setAtualizadoEm(Instant.now());
        veiculo.validar();
        veiculoRepository.updateVeiculo(empresaId, veiculo);
    }

    @Override
    public ImpostosVeiculo findImpostosVeiculo(int empresaId, UUID veiculoId) {
        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        if (veiculo.getPrecoVenda() == null) {
            return new ImpostosVeiculo(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }
        BigDecimal baseLucro = veiculo.getPrecoVenda().subtract(veiculo.getPrecoCompra());
        BigDecimal taxableMargem = baseLucro.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : baseLucro;
        CalculadoraVendasVeiculo.ImpostoBreakdown impostos = vendasCalculator.calcularImpostos(
                veiculo.getPrecoVenda(),
                taxableMargem
        );
        return new ImpostosVeiculo(
                impostos.icms(),
                impostos.pis(),
                impostos.cofins(),
                impostos.csll(),
                impostos.irpj(),
                impostos.totalImpostos()
        );
    }

    @Override
    public List<ResumoVeiculo> listarVeiculos(int empresaId, StatusVeiculo status, String query, Boolean isEmServico, int page, int size) {
        if (size > 20) {
            size = 20;
        }

        List<Veiculo> veiculos = veiculoRepository.findVeiculoByFilter(empresaId, status, query, isEmServico, page, size);
        return veiculos.stream()
                .map(veiculo -> {
                    BigDecimal totalServicos = veiculoRepository.findVeiculoTotalServicosByVeiculoId(empresaId, veiculo.getId());
                    BigDecimal custoTotal = veiculo.getPrecoCompra()
                            .add(veiculo.getCustoFrete())
                            .add(totalServicos);
                    BigDecimal precoVenda = veiculo.getPrecoVenda();
                    BigDecimal margemLucro = precoVenda == null
                            ? null
                            : precoVenda.subtract(custoTotal);
                    Integer diasTempoCompra = veiculo.calcularTempoCompra();

                    boolean servicoOpened = servicoRepository.existsOpenServicoByVeiculoId(empresaId, veiculo.getId());
                    return new ResumoVeiculo(
                            veiculo.getId(),
                            veiculo.getPlaca(),
                            veiculo.getMarca(),
                            veiculo.getModelo(),
                            veiculo.getAno(),
                            veiculo.getStatus(),
                            servicoOpened,
                            precoVenda,
                            custoTotal,
                            margemLucro,
                            diasTempoCompra,
                            totalServicos,
                            servicoNoVeiculoService.calcularTotalDiasServico(empresaId, veiculo.getId())
                    );
                })
                .filter(veiculo -> isEmServico == null || veiculo.emServico() == isEmServico)
                .toList();
    }

    @Override
    public long countVeiculos(int empresaId, StatusVeiculo status, String query, Boolean emServico) {
        return veiculoRepository.countVeiculoByFilter(empresaId, status, query, emServico);
    }

    @Override
    public void updateVeiculo(int empresaId,
                              UUID veiculoId,
                              int ano,
                              String cor,
                              String modelo,
                              String marca,
                              OrigemFornecedor origemFornecedor,
                              BigDecimal precoCompra,
                              BigDecimal custoFrete,
                              BigDecimal comissaoCompra,
                              UUID notaFiscalDocumentoId,
                              UUID pagamentoComprovanteDocumentoId,
                              BigDecimal valorFipe) {

        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        validateDocumentoLink(empresaId, veiculoId, notaFiscalDocumentoId);
        validateDocumentoLink(empresaId, veiculoId, pagamentoComprovanteDocumentoId);
        Instant now = Instant.now();
        veiculo.updateDetails(
                ano,
                cor,
                modelo,
                marca,
                origemFornecedor,
                precoCompra,
                custoFrete,
                comissaoCompra
        );
        veiculo.validar();
        veiculo.normalizar();
        resolveCor(empresaId, veiculo.getCor(), now);
        Marca marcaEntity = resolveMarca(empresaId, veiculo.getMarca(), now);
        ModeloVeiculo modeloEntity = resolveModelo(empresaId, marcaEntity.getId(), veiculo.getModelo(), now);
        veiculo.updateLinkedDocumentos(notaFiscalDocumentoId, pagamentoComprovanteDocumentoId);
        veiculo.setMarcaId(marcaEntity.getId());
        veiculo.setModeloId(modeloEntity.getId());
        veiculo.setValorFipe(valorFipe);
        veiculo.setAtualizadoEm(Instant.now());
        veiculo.ensureDistribuicaoInvariant();
        stampTaxaComissao(empresaId, veiculo);
        if (veiculo.isVendido()) {
            stampDataVenda(veiculo);
        }
        veiculoRepository.updateVeiculo(empresaId, veiculo);
    }

    @Override
    public void transitionStatus(int empresaId, UUID veiculoId, StatusVeiculo targetStatus, UUID parceiroAtribuidoId) {
        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        if (!veiculo.isStatusTransitionAllowed(targetStatus)) {
            throw new InvalidStateException("Transição de status inválida.");
        }
        if (targetStatus == StatusVeiculo.DISTRIBUIDO && parceiroAtribuidoId == null && veiculo.getParceiroAtribuidoId() == null) {
            throw new InvalidStateException("O parceiro atribuído é obrigatório ao distribuir um veículo.");
        }
        if (targetStatus == StatusVeiculo.VENDIDO && veiculo.getPrecoVenda() == null) {
            throw new InvalidStateException("O preço de venda é obrigatório antes de marcar como vendido.");
        }
        if ((targetStatus == StatusVeiculo.DISTRIBUIDO || targetStatus == StatusVeiculo.VENDIDO)
                && parceiroAtribuidoId != null) {
            parceiroRepository.findParceiroById(empresaId, parceiroAtribuidoId)
                    .orElseThrow(() -> new NotFoundException("Parceiro não encontrado"));
        }
        UUID parceiro = parceiroAtribuidoId != null ? parceiroAtribuidoId : veiculo.getParceiroAtribuidoId();
        veiculo.transitionStatus(targetStatus, parceiro);
        if (targetStatus == StatusVeiculo.DISTRIBUIDO) {
            if (veiculo.getDistribuidoEm() == null) {
                veiculo.setDistribuidoEm(Instant.now());
            }
            if (parceiro != null) {
                parceiroRepository.findParceiroById(empresaId, parceiro)
                        .ifPresent(p -> veiculo.setTaxaComissaoVenda(p.getTaxaComissao()));
            }
        }
        if (targetStatus == StatusVeiculo.VENDIDO) {
            stampDataVenda(veiculo);
            if (parceiro != null) {
                parceiroRepository.findParceiroById(empresaId, parceiro)
                        .ifPresent(p -> veiculo.setTaxaComissaoVenda(p.getTaxaComissao()));
            } else {
                stampTaxaComissao(empresaId, veiculo);
            }
        }
        veiculo.setAtualizadoEm(Instant.now());
        veiculo.ensureDistribuicaoInvariant();
        veiculoRepository.updateVeiculo(empresaId, veiculo);
    }

    @Override
    public void atribuirParceiro(int empresaId, UUID veiculoId, UUID parceiroId) {
        Veiculo veiculo = veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        Parceiro parceiro = parceiroRepository.findParceiroById(empresaId, parceiroId)
                .orElseThrow(() -> new NotFoundException("Parceiro não encontrado"));
        veiculo.atribuirParceiro(parceiro.getId());
        if (veiculo.getDistribuidoEm() == null) {
            veiculo.setDistribuidoEm(Instant.now());
        }
        veiculo.setTaxaComissaoVenda(parceiro.getTaxaComissao());
        veiculo.setAtualizadoEm(Instant.now());
        veiculo.ensureDistribuicaoInvariant();
        veiculoRepository.updateVeiculo(empresaId, veiculo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVeiculo(int empresaId, UUID veiculoId) {
        veiculoRepository.findVeiculoById(empresaId, veiculoId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));

        List<String> storageKeys = documentoRepository.findStorageKeyByVeiculoId(empresaId, veiculoId);
        deleteFormStorage(veiculoId, storageKeys);
        pagamentoService.deletePagamentosByVeiculoId(empresaId, veiculoId);
        historicoVeiculoEmServicoRepository.deleteByVeiculoId(empresaId, veiculoId);
        servicoRepository.deleteServicosByVeiculoId(empresaId, veiculoId);
        veiculoRepository.deleteVeiculo(empresaId, veiculoId);
    }

    private void deleteFormStorage(UUID veiculoId, List<String> storageKeys) {
        for (var storageKey : storageKeys) {
            try {
                documentoStorage.delete(storageKey);
            } catch (Exception e) {
                log.warn("Objeto de armazenamento não encontrado ao excluir documento. veiculoId={}, storageKey={}",
                        veiculoId, storageKey);
                throw e;
            }
        }
    }

    private Marca resolveMarca(int empresaId, String marca, Instant now) {
        if (!StringUtils.hasText(marca)) {
            throw new IllegalArgumentException("marca: obrigatório.");
        }
        return marcaRepository.findMarcaByNome(empresaId, marca)
                .orElseGet(() -> marcaRepository.saveMarca(empresaId, new Marca(
                        UuidGenerator.generate(),
                        empresaId,
                        marca,
                        now,
                        now
                )));
    }

    private void resolveCor(int empresaId, String cor, Instant now) {
        if (!StringUtils.hasText(cor)) {
            throw new IllegalArgumentException("cor: obrigatório.");
        }
        corRepository.findCorByNome(empresaId, cor)
                .orElseGet(() -> corRepository.saveCor(empresaId, new Cor(
                        UuidGenerator.generate(),
                        empresaId,
                        cor,
                        now,
                        now
                )));
    }

    private ModeloVeiculo resolveModelo(int empresaId, UUID marcaId, String modelo, Instant now) {
        if (!StringUtils.hasText(modelo)) {
            throw new IllegalArgumentException("modelo: obrigatório.");
        }
        return modeloVeiculoRepository.findModeloByMarcaIdAndNome(empresaId, marcaId, modelo)
                .orElseGet(() -> modeloVeiculoRepository.saveModelo(empresaId, new ModeloVeiculo(
                        UuidGenerator.generate(),
                        empresaId,
                        marcaId,
                        modelo,
                        now,
                        now
                )));
    }



    private void validateDocumentoLink(int empresaId, UUID veiculoId, UUID documentoId) {
        if (documentoId == null) {
            return;
        }
        documentoRepository.findDocumentoById(empresaId, documentoId)
                .filter(documento -> documento.getVeiculoId().equals(veiculoId))
                .orElseThrow(() -> new NotFoundException("Documento não encontrado para o veículo."));
    }

    private String resolveNomeParceiro(int empresaId, UUID parceiroId) {
        if (parceiroId == null) {
            return null;
        }
        Optional<Parceiro> parceiro = parceiroRepository.findParceiroById(empresaId, parceiroId);
        return parceiro.map(Parceiro::getNome).orElse(null);
    }

    private void stampDataVenda(Veiculo veiculo) {
        if (veiculo.getDataVenda() == null) {
            veiculo.setDataVenda(LocalDate.now());
        }
    }

    private void stampTaxaComissao(int empresaId, Veiculo veiculo) {
        UUID parceiroId = veiculo.getParceiroAtribuidoId();
        if (parceiroId != null) {
            parceiroRepository.findParceiroById(empresaId, parceiroId)
                    .ifPresent(p -> veiculo.setTaxaComissaoVenda(p.getTaxaComissao()));
        }
    }

}
