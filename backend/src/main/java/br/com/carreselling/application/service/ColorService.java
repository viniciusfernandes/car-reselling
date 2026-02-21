package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.ColorSummary;
import br.com.carreselling.domain.repository.ColorRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ColorService implements IColorService {

    private final ColorRepository colorRepository;

    public ColorService(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @Override
    public List<ColorSummary> listColors() {
        return colorRepository.findColors()
            .stream()
            .map(color -> new ColorSummary(color.getId(), color.getName()))
            .toList();
    }
}
