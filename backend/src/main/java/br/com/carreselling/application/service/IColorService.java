package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.ColorSummary;
import java.util.List;

public interface IColorService {

    List<ColorSummary> listColors(int companyId);
}
