package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.Marca;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarcaRepository {

    Marca saveMarca(int empresaId, Marca marca);

    List<Marca> findMarcas(int empresaId);

    Optional<Marca> findMarcaById(int empresaId, UUID id);

    Optional<Marca> findMarcaByNome(int empresaId, String nome);
}
