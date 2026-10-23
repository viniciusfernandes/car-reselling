package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoDocumento;
import br.com.revendacarros.domain.model.TipoDocumento;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IDocumentoService {

    UUID uploadDocumento(int empresaId, UUID veiculoId, TipoDocumento tipoDocumento, MultipartFile file);

    List<ResumoDocumento> listarDocumentos(int empresaId, UUID veiculoId);

    Resource downloadDocumento(int empresaId, UUID veiculoId, UUID documentoId);

    ResumoDocumento getDocumento(int empresaId, UUID veiculoId, UUID documentoId);

    void deleteDocumento(int empresaId, UUID veiculoId, UUID documentoId);
}
