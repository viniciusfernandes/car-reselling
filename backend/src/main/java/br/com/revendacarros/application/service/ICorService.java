package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoCor;
import java.util.List;

public interface ICorService {

    List<ResumoCor> listarCores(int empresaId);
}
