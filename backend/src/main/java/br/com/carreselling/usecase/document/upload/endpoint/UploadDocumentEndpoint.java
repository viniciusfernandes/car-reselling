package br.com.carreselling.usecase.document.upload.endpoint;

import br.com.carreselling.application.service.IDocumentService;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.domain.model.DocumentType;
import br.com.carreselling.usecase.document.upload.contract.UploadDocumentResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class UploadDocumentEndpoint {

    private final IDocumentService documentService;

    public UploadDocumentEndpoint(IDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/{vehicleId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadDocumentResponse>> upload(@PathVariable UUID vehicleId,
                                                                      @RequestParam DocumentType documentType,
                                                                      @RequestParam("file") MultipartFile file) {
        UUID documentId = documentService.uploadDocument(vehicleId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(new UploadDocumentResponse(documentId)));
    }
}
