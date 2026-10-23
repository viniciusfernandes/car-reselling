package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoModeloVeiculo;
import java.util.List;
import java.util.UUID;

public interface IModeloVeiculoService {

    List<ResumoModeloVeiculo> listarModelosByMarcaId(int empresaId, UUID marcaId);
}
