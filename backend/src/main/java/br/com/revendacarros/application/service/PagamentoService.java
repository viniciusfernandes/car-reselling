package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoDocumentoPagamento;
import br.com.revendacarros.application.service.model.ResumoPagamento;
import br.com.revendacarros.common.UuidGenerator;
import br.com.revendacarros.domain.exception.NotFoundException;
import br.com.revendacarros.domain.model.*;
import br.com.revendacarros.domain.repository.DocumentoPagamentoRepository;
import br.com.revendacarros.domain.repository.PagamentoRepository;
import br.com.revendacarros.domain.repository.VeiculoRepository;
import br.com.revendacarros.infrastructure.storage.DocumentoPagamentoStorage;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PagamentoService implements IPagamentoService {

    private static final long TAMANHO_MAXIMO_ARQUIVO_BYTES = 20L * 1024 * 1024;

    private final PagamentoRepository pagamentoRepository;
    private final DocumentoPagamentoRepository documentoPagamentoRepository;
    private final DocumentoPagamentoStorage documentoPagamentoStorage;
    private final VeiculoRepository veiculoRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository,
                          DocumentoPagamentoRepository documentoPagamentoRepository,
                          DocumentoPagamentoStorage documentoPagamentoStorage,
                          VeiculoRepository veiculoRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.documentoPagamentoRepository = documentoPagamentoRepository;
        this.documentoPagamentoStorage = documentoPagamentoStorage;
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public UUID criarPagamento(int empresaId,
                              TipoPagamento tipoPagamento,
                              String descricao,
                              BigDecimal valor,
                              LocalDate dataPagamento,
                              String veiculoPlaca,
                              String observacoes) {
        UUID resolvedVeiculoId = resolveVeiculoForGarantia(empresaId, tipoPagamento, veiculoPlaca);
        String normalizedDescricao = descricao != null ? descricao.toUpperCase().trim() : null;
        UUID id = UuidGenerator.generate();
        Pagamento pagamento = new Pagamento(id, empresaId, tipoPagamento, normalizedDescricao, valor, dataPagamento,
                resolvedVeiculoId, observacoes, Instant.now(), null);
        pagamentoRepository.savePagamento(empresaId, pagamento);
        return id;
    }

    @Override
    public List<ResumoPagamento> listarPagamentos(int empresaId, TipoPagamento tipoPagamento, Integer anoPagamento, Integer mesPagamento, String placa) {
        return pagamentoRepository.findPagamentos(empresaId, tipoPagamento, anoPagamento, mesPagamento, placa)
                .stream()
                .map(this::toResumo)
                .toList();
    }

    @Override
    public ResumoPagamento getPagamento(int empresaId, UUID id) {
        Pagamento pagamento = pagamentoRepository.findPagamentoById(empresaId, id)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        return toResumo(pagamento);
    }

    @Override
    public void updatePagamento(int empresaId,
                              UUID id,
                              TipoPagamento tipoPagamento,
                              String descricao,
                              BigDecimal valor,
                              LocalDate dataPagamento,
                              String veiculoPlaca,
                              String observacoes) {
        Pagamento pagamento = pagamentoRepository.findPagamentoById(empresaId, id)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        UUID resolvedVeiculoId = resolveVeiculoForGarantia(empresaId, tipoPagamento, veiculoPlaca);
        String normalizedDescricao = descricao != null ? descricao.toUpperCase().trim() : null;
        pagamento.update(tipoPagamento, normalizedDescricao, valor, dataPagamento, resolvedVeiculoId, observacoes);
        pagamentoRepository.updatePagamento(empresaId, pagamento);
    }

    @Override
    public List<String> listarDescricoes(int empresaId, TipoPagamento tipoPagamento) {
        return pagamentoRepository.findDescricoesDistintas(empresaId, tipoPagamento);
    }

    private UUID resolveVeiculoForGarantia(int empresaId, TipoPagamento tipoPagamento, String veiculoPlaca) {
        if (tipoPagamento != TipoPagamento.GARANTIA) {
            return null;
        }
        if (veiculoPlaca == null || veiculoPlaca.isBlank()) {
            throw new IllegalArgumentException("A placa do veículo é obrigatória para pagamentos de GARANTIA.");
        }
        Veiculo veiculo = veiculoRepository.findVeiculoByPlaca(empresaId, veiculoPlaca.toUpperCase().trim())
                .orElseThrow(() -> new NotFoundException(
                        "Veículo com placa '" + veiculoPlaca + "' não encontrado."));
        if (veiculo.getStatus() != StatusVeiculo.VENDIDO) {
            throw new IllegalArgumentException(
                    "Veículo com placa '" + veiculoPlaca + "' não está vendido. Apenas veículos vendidos podem ter lançamentos de garantia.");
        }
        return veiculo.getId();
    }

    @Override
    public void deletePagamento(int empresaId, UUID id) {
        Pagamento pagamento = pagamentoRepository.findPagamentoById(empresaId, id)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        List<DocumentoPagamento> documentos = documentoPagamentoRepository.findDocumentosPagamentoByPagamentoId(empresaId, pagamento.id);
        for (DocumentoPagamento doc : documentos) {
            documentoPagamentoStorage.delete(doc.storageKey);
        }
        documentoPagamentoRepository.deleteDocumentosPagamentoByPagamentoId(empresaId, id);
        pagamentoRepository.deletePagamento(empresaId, id);
    }

    @Override
    public void deletePagamentosByVeiculoId(int empresaId, UUID veiculoId) {
        List<Pagamento> pagamentos = pagamentoRepository.findPagamentosByVeiculoId(empresaId, veiculoId);
        for (Pagamento pagamento : pagamentos) {
            List<DocumentoPagamento> documentos = documentoPagamentoRepository.findDocumentosPagamentoByPagamentoId(empresaId, pagamento.id);
            for (DocumentoPagamento doc : documentos) {
                documentoPagamentoStorage.delete(doc.storageKey);
            }
            documentoPagamentoRepository.deleteDocumentosPagamentoByPagamentoId(empresaId, pagamento.id);
        }
        pagamentoRepository.deletePagamentosByVeiculoId(empresaId, veiculoId);
    }

    @Override
    public UUID uploadDocumentoPagamento(int empresaId, UUID pagamentoId, MultipartFile file) {
        pagamentoRepository.findPagamentoById(empresaId, pagamentoId)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        if (file.getSize() > TAMANHO_MAXIMO_ARQUIVO_BYTES) {
            throw new IllegalArgumentException("O arquivo excede o tamanho máximo.");
        }
        UUID documentoId = UuidGenerator.generate();
        String nomeOriginalArquivo = file.getOriginalFilename() == null ? "documento" : file.getOriginalFilename();
        String storageKey;
        try {
            storageKey = documentoPagamentoStorage.store(pagamentoId, documentoId, nomeOriginalArquivo, file.getInputStream());
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao armazenar documento de pagamento");
        }
        DocumentoPagamento documento = new DocumentoPagamento(
                documentoId,
                empresaId,
                pagamentoId,
                nomeOriginalArquivo,
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getSize(),
                storageKey,
                Instant.now(),
                "system"
        );
        documentoPagamentoRepository.saveDocumentoPagamento(empresaId, documento);
        return documentoId;
    }

    @Override
    public List<ResumoDocumentoPagamento> listarDocumentosPagamento(int empresaId, UUID pagamentoId) {
        pagamentoRepository.findPagamentoById(empresaId, pagamentoId)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        return documentoPagamentoRepository.findDocumentosPagamentoByPagamentoId(empresaId, pagamentoId)
                .stream()
                .map(doc -> new ResumoDocumentoPagamento(
                        doc.id,
                        doc.pagamentoId,
                        doc.nomeOriginalArquivo,
                        doc.contentType,
                        doc.tamanhoBytes,
                        doc.enviadoEm
                ))
                .toList();
    }

    @Override
    public Resource downloadDocumentoPagamento(int empresaId, UUID pagamentoId, UUID documentoId) {
        DocumentoPagamento documento = getDocumentoPagamentoEntity(empresaId, pagamentoId, documentoId);
        return documentoPagamentoStorage.load(documento.storageKey);
    }

    @Override
    public void deleteDocumentoPagamento(int empresaId, UUID pagamentoId, UUID documentoId) {
        DocumentoPagamento documento = getDocumentoPagamentoEntity(empresaId, pagamentoId, documentoId);
        documentoPagamentoStorage.delete(documento.storageKey);
        documentoPagamentoRepository.deleteDocumentoPagamento(empresaId, documentoId);
    }

    private DocumentoPagamento getDocumentoPagamentoEntity(int empresaId, UUID pagamentoId, UUID documentoId) {
        DocumentoPagamento documento = documentoPagamentoRepository.findDocumentoPagamentoById(empresaId, documentoId)
                .orElseThrow(() -> new NotFoundException("Documento de pagamento não encontrado"));
        if (!documento.pagamentoId.equals(pagamentoId)) {
            throw new NotFoundException("Documento não encontrado para o pagamento");
        }
        return documento;
    }

    private ResumoPagamento toResumo(Pagamento pagamento) {
        return new ResumoPagamento(
                pagamento.id,
                pagamento.tipoPagamento,
                pagamento.descricao,
                pagamento.valor,
                pagamento.dataPagamento,
                pagamento.veiculoId,
                pagamento.veiculoPlaca,
                pagamento.observacoes,
                pagamento.criadoEm,
                pagamento.atualizadoEm
        );
    }
}
