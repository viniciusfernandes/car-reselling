package br.com.revendacarros.domain.model;

import br.com.revendacarros.domain.exception.InvalidStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public class Veiculo {

    private final UUID id;
    private final int empresaId;
    private final String placa;
    private String renavam;
    private String chassi;
    private int ano;
    private String cor;
    private String modelo;
    private String marca;
    private UUID marcaId;
    private UUID modeloId;
    private OrigemFornecedor origemFornecedor;
    private BigDecimal precoCompra;
    private BigDecimal custoFrete;
    private BigDecimal comissaoCompra;
    private BigDecimal precoVenda;
    private UUID documentoComprovantePagamentoCompraId;
    private UUID documentoNotaFiscalCompraId;
    private StatusVeiculo status;
    private UUID parceiroAtribuidoId;
    private Instant distribuidoEm;
    private LocalDate dataVenda;
    private BigDecimal taxaComissaoVenda;
    private BigDecimal valorFipe;
    private Instant criadoEm;
    private Instant atualizadoEm;

    public Veiculo(UUID id,
                   int empresaId,
                   String placa,
                   String renavam,
                   String chassi,
                   int ano,
                   String cor,
                   String modelo,
                   String marca,
                   UUID marcaId,
                   UUID modeloId,
                   OrigemFornecedor origemFornecedor,
                   BigDecimal precoCompra,
                   BigDecimal custoFrete,
                   BigDecimal comissaoCompra,
                   BigDecimal precoVenda,
                   BigDecimal valorFipe,
                   UUID documentoComprovantePagamentoCompraId,
                   UUID documentoNotaFiscalCompraId,
                   StatusVeiculo status,
                   UUID parceiroAtribuidoId,
                   Instant distribuidoEm,
                   LocalDate dataVenda,
                   BigDecimal taxaComissaoVenda,
                   Instant criadoEm,
                   Instant atualizadoEm) {
        this.id = id;
        this.empresaId = empresaId;
        this.placa = placa;
        this.renavam = renavam;
        this.chassi = chassi;
        this.ano = ano;
        this.cor = cor;
        this.modelo = modelo;
        this.marca = marca;
        this.marcaId = marcaId;
        this.modeloId = modeloId;
        this.origemFornecedor = origemFornecedor;
        this.precoCompra = precoCompra;
        this.custoFrete = custoFrete;
        this.comissaoCompra = comissaoCompra;
        this.precoVenda = precoVenda;
        this.valorFipe = valorFipe;
        this.documentoComprovantePagamentoCompraId = documentoComprovantePagamentoCompraId;
        this.documentoNotaFiscalCompraId = documentoNotaFiscalCompraId;
        this.status = status;
        this.parceiroAtribuidoId = parceiroAtribuidoId;
        this.distribuidoEm = distribuidoEm;
        this.dataVenda = dataVenda;
        this.taxaComissaoVenda = taxaComissaoVenda;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void updateDetails(int ano,
                              String cor,
                              String modelo,
                              String marca,
                              OrigemFornecedor origemFornecedor,
                              BigDecimal precoCompra,
                              BigDecimal custoFrete,
                              BigDecimal comissaoCompra) {
        this.ano = ano;
        this.cor = cor;
        this.modelo = modelo;
        this.marca = marca;
        this.origemFornecedor = origemFornecedor;
        this.precoCompra = precoCompra;
        this.custoFrete = custoFrete;
        this.comissaoCompra = comissaoCompra;
    }

    public void updatePrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public void updateLinkedDocumentos(UUID notaFiscalDocumentoId, UUID pagamentoComprovanteDocumentoId) {
        this.documentoNotaFiscalCompraId = notaFiscalDocumentoId;
        this.documentoComprovantePagamentoCompraId = pagamentoComprovanteDocumentoId;
    }

    public void transitionStatus(StatusVeiculo targetStatus) {
        transitionStatus(targetStatus, null);
    }

    public void transitionStatus(StatusVeiculo targetStatus, UUID parceiroId) {
        if (targetStatus == StatusVeiculo.DISTRIBUIDO && parceiroId == null) {
            throw new InvalidStateException("O parceiro atribuído é obrigatório ao distribuir um veículo.");
        }
        if (targetStatus == StatusVeiculo.DISTRIBUIDO || targetStatus == StatusVeiculo.VENDIDO) {
            if (parceiroId != null) {
                this.parceiroAtribuidoId = parceiroId;
            }
        } else {
            this.parceiroAtribuidoId = null;
        }
        this.status = targetStatus;
    }

    public void atribuirParceiro(UUID parceiroId) {
        if (status != StatusVeiculo.PRONTO_PARA_DISTRIBUICAO) {
            throw new InvalidStateException("O veículo deve estar pronto para distribuição.");
        }
        this.parceiroAtribuidoId = parceiroId;
        this.status = StatusVeiculo.DISTRIBUIDO;
    }

    public void ensureServicosEditable() {
        if (status == StatusVeiculo.VENDIDO) {
            throw new InvalidStateException("Os serviços são somente leitura após a venda.");
        }
    }

    public void ensureDistribuicaoInvariant() {
        if (status == StatusVeiculo.DISTRIBUIDO && parceiroAtribuidoId == null) {
            throw new InvalidStateException("O parceiro atribuído é obrigatório quando o veículo está distribuído.");
        }
        if (status != StatusVeiculo.DISTRIBUIDO && status != StatusVeiculo.VENDIDO && parceiroAtribuidoId != null) {
            throw new InvalidStateException("O parceiro atribuído só pode ser definido quando o veículo está distribuído.");
        }
    }

    public UUID getId() {
        return id;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public String getPlaca() {
        return placa;
    }

    public String getRenavam() {
        return renavam;
    }

    public String getChassi() {
        return chassi;
    }

    public int getAno() {
        return ano;
    }

    public String getCor() {
        return cor;
    }

    public String getModelo() {
        return modelo;
    }

    public String getMarca() {
        return marca;
    }

    public UUID getMarcaId() {
        return marcaId;
    }

    public UUID getModeloId() {
        return modeloId;
    }

    public OrigemFornecedor getOrigemFornecedor() {
        return origemFornecedor;
    }

    public BigDecimal getPrecoCompra() {
        return precoCompra;
    }

    public BigDecimal getCustoFrete() {
        return custoFrete;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public BigDecimal getComissaoCompra() {
        return comissaoCompra;
    }

    public UUID getDocumentoComprovantePagamentoCompraId() {
        return documentoComprovantePagamentoCompraId;
    }

    public UUID getDocumentoNotaFiscalCompraId() {
        return documentoNotaFiscalCompraId;
    }

    public StatusVeiculo getStatus() {
        return status;
    }

    public UUID getParceiroAtribuidoId() {
        return parceiroAtribuidoId;
    }

    public Instant getDistribuidoEm() {
        return distribuidoEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setRenavam(String renavam) {
        this.renavam = renavam;
    }

    public void setChassi(String chassi) {
        this.chassi = chassi;
    }

    public void setMarcaId(UUID marcaId) {
        this.marcaId = marcaId;
    }

    public void setModeloId(UUID modeloId) {
        this.modeloId = modeloId;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setParceiroAtribuidoId(UUID parceiroAtribuidoId) {
        this.parceiroAtribuidoId = parceiroAtribuidoId;
    }

    public void setDistribuidoEm(Instant distribuidoEm) {
        this.distribuidoEm = distribuidoEm;
    }

    public LocalDate getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDate dataVenda) {
        this.dataVenda = dataVenda;
    }

    public BigDecimal getTaxaComissaoVenda() {
        return taxaComissaoVenda;
    }

    public void setTaxaComissaoVenda(BigDecimal taxaComissaoVenda) {
        this.taxaComissaoVenda = taxaComissaoVenda;
    }

    public BigDecimal getValorFipe() {
        return valorFipe;
    }

    public void setValorFipe(BigDecimal valorFipe) {
        this.valorFipe = valorFipe;
    }

    public boolean isStatusTransitionAllowed(StatusVeiculo target) {
        return status == null || status.isTransitionAllowed(target);
    }

    public boolean isVendido() {
        return status == StatusVeiculo.VENDIDO;
    }

    public int calcularTempoCompra() {
        if (criadoEm == null) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        LocalDate dataInicio = criadoEm
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate dataFim = dataVenda != null ? dataVenda : now;
        return (int) ChronoUnit.DAYS.between(dataInicio, dataFim);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Veiculo veiculo = (Veiculo) o;
        return Objects.equals(id, veiculo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
