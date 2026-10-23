package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.ModeloVeiculo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModeloVeiculoRepository {

    ModeloVeiculo saveModelo(int empresaId, ModeloVeiculo modelo);

    List<ModeloVeiculo> findModelosByMarcaId(int empresaId, UUID marcaId);

    Optional<ModeloVeiculo> findModeloById(int empresaId, UUID id);

    Optional<ModeloVeiculo> findModeloByMarcaIdAndNome(int empresaId, UUID marcaId, String nome);
}
