package br.com.carreselling.usecase.document.download.endpoint;

import br.com.carreselling.application.service.IDocumentService;
import br.com.carreselling.application.service.model.DocumentSummary;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class DownloadDocumentEndpoint {

    private final IDocumentService documentService;

    public DownloadDocumentEndpoint(IDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{vehicleId}/documents/{documentId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID vehicleId, @PathVariable UUID documentId) {
        DocumentSummary summary = documentService.getDocument(vehicleId, documentId);
        Resource resource = documentService.downloadDocument(vehicleId, documentId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(summary.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + summary.originalFileName() + "\"")
            .body(resource);
    }
}
