package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.Cor;
import java.util.List;
import java.util.Optional;

public interface CorRepository {

    Cor saveCor(int empresaId, Cor cor);

    List<Cor> findCores(int empresaId);

    Optional<Cor> findCorByNome(int empresaId, String nome);
}
