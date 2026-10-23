package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.DetalheVeiculo;
import br.com.revendacarros.application.service.model.ResumoVeiculo;
import br.com.revendacarros.application.service.model.ImpostosVeiculo;
import br.com.revendacarros.domain.model.OrigemFornecedor;
import br.com.revendacarros.domain.model.StatusVeiculo;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IVeiculoService {

    UUID criarVeiculo(int empresaId,
                       String placa,
                       String renavam,
                       String chassi,
                       int ano,
                       String cor,
                       String modelo,
                       String marca,
                       OrigemFornecedor origemFornecedor,
                       BigDecimal precoCompra,
                       BigDecimal custoFrete,
                       BigDecimal comissaoCompra,
                       BigDecimal valorFipe);

    DetalheVeiculo getVeiculo(int empresaId, UUID veiculoId);

    List<ResumoVeiculo> listarVeiculos(int empresaId, StatusVeiculo status, String query, Boolean emServico, int page, int size);

    long countVeiculos(int empresaId, StatusVeiculo status, String query, Boolean emServico);

    void updateVeiculo(int empresaId,
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
                       BigDecimal valorFipe);

    void updatePrecoVenda(int empresaId, UUID veiculoId, BigDecimal precoVenda);

    ImpostosVeiculo getImpostosVeiculo(int empresaId, UUID veiculoId);

    void transitionStatus(int empresaId, UUID veiculoId, StatusVeiculo targetStatus, UUID parceiroAtribuidoId);

    void atribuirParceiro(int empresaId, UUID veiculoId, UUID parceiroId);

    void deleteVeiculo(int empresaId, UUID veiculoId);
}
