package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoCor;
import br.com.revendacarros.domain.repository.CorRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CorService implements ICorService {

    private final CorRepository corRepository;

    public CorService(CorRepository corRepository) {
        this.corRepository = corRepository;
    }

    @Override
    public List<ResumoCor> listarCores(int empresaId) {
        return corRepository.findCores(empresaId)
            .stream()
            .map(cor -> new ResumoCor(cor.getId(), cor.getNome()))
            .toList();
    }
}
