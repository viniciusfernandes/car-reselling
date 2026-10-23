package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoMarca;
import java.util.List;

public interface IMarcaService {

    List<ResumoMarca> listarMarcas(int empresaId);
}
