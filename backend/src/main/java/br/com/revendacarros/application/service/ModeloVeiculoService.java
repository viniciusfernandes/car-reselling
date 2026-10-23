package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoModeloVeiculo;
import br.com.revendacarros.domain.repository.ModeloVeiculoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ModeloVeiculoService implements IModeloVeiculoService {

    private final ModeloVeiculoRepository modeloVeiculoRepository;

    public ModeloVeiculoService(ModeloVeiculoRepository modeloVeiculoRepository) {
        this.modeloVeiculoRepository = modeloVeiculoRepository;
    }

    @Override
    public List<ResumoModeloVeiculo> listarModelosByMarcaId(int empresaId, UUID marcaId) {
        return modeloVeiculoRepository.findModelosByMarcaId(empresaId, marcaId)
            .stream()
            .map(modelo -> new ResumoModeloVeiculo(modelo.getId(), modelo.getNome()))
            .toList();
    }
}
