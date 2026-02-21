package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Color;
import java.util.List;
import java.util.Optional;

public interface ColorRepository {

    Color saveColor(Color color);

    List<Color> findColors();

    Optional<Color> findColorByName(String name);
}
