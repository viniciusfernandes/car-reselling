package br.com.carreselling.usecase.document.list.endpoint;

import br.com.carreselling.application.service.IDocumentService;
import br.com.carreselling.application.service.model.DocumentSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.usecase.document.list.contract.DocumentItem;
import br.com.carreselling.usecase.document.list.contract.DocumentListResponse;
import br.com.carreselling.usecase.document.list.mapping.DocumentListMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
public class ListDocumentEndpoint {

    private final IDocumentService documentService;

    public ListDocumentEndpoint(IDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{vehicleId}/documents")
    public ApiResponse<DocumentListResponse> list(@PathVariable UUID vehicleId) {
        List<DocumentSummary> documents = documentService.listDocuments(vehicleId);
        List<DocumentItem> items = documents.stream()
            .map(DocumentListMapper::toItem)
            .toList();
        return new ApiResponse<>(new DocumentListResponse(items));
    }
}
