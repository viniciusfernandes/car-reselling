package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoMarca;
import br.com.revendacarros.domain.repository.MarcaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MarcaService implements IMarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Override
    public List<ResumoMarca> listarMarcas(int empresaId) {
        return marcaRepository.findMarcas(empresaId)
            .stream()
            .map(marca -> new ResumoMarca(marca.getId(), marca.getNome()))
            .toList();
    }
}
