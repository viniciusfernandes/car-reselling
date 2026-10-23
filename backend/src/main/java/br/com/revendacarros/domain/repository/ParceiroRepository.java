package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.Parceiro;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParceiroRepository {

    Parceiro saveParceiro(int empresaId, Parceiro parceiro);

    Parceiro updateParceiro(int empresaId, Parceiro parceiro);

    /** Retorna apenas parceiros com habilitado = true. */
    List<Parceiro> findParceirosHabilitados(int empresaId);

    Optional<Parceiro> findParceiroById(int empresaId, UUID id);

    Optional<Parceiro> findParceiroByNome(int empresaId, String nome);

    void setHabilitado(int empresaId, UUID id, boolean habilitado);
}
