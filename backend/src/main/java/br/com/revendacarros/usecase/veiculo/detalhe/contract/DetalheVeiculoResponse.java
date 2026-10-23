package br.com.revendacarros.usecase.veiculo.detalhe.contract;

import br.com.revendacarros.domain.model.OrigemFornecedor;
import br.com.revendacarros.domain.model.StatusVeiculo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DetalheVeiculoResponse(UUID id,
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
                                    BigDecimal precoVenda,
                                    BigDecimal valorFipe,
                                    UUID documentoNotaFiscalCompraId,
                                    UUID documentoComprovantePagamentoCompraId,
                                    StatusVeiculo status,
                                    boolean emServico,
                                    UUID parceiroAtribuidoId,
                                    String nomeParceiroAtribuido,
                                    BigDecimal totalServicos,
                                    BigDecimal custoTotal,
                                    int documentosCount,
                                    Instant criadoEm,
                                    Instant atualizadoEm,
                                    Instant distribuidoEm) {
}
