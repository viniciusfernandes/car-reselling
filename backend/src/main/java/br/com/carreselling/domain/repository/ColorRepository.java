package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Color;
import java.util.List;
import java.util.Optional;

public interface ColorRepository {

    Color saveColor(int companyId, Color color);

    List<Color> findColors(int companyId);

    Optional<Color> findColorByName(int companyId, String name);
}
