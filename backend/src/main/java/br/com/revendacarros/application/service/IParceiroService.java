package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoHistoricoParceiro;
import br.com.revendacarros.application.service.model.ResumoParceiro;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IParceiroService {

    UUID criarParceiro(int empresaId, String nome, String cidade, String telefone, String email, BigDecimal taxaComissao);

    List<ResumoParceiro> listarParceiros(int empresaId);

    ResumoParceiro getParceiro(int empresaId, UUID id);

    void updateParceiro(int empresaId, UUID id, String nome, String cidade, String telefone, String email,
                       BigDecimal taxaComissao, String alteradoPor);

    List<ResumoHistoricoParceiro> getHistoricoParceiro(int empresaId, UUID id);

    void desabilitarParceiro(int empresaId, UUID id);
}
