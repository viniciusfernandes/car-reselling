package br.com.carreselling.usecase.color.list.endpoint;

import br.com.carreselling.application.service.IColorService;
import br.com.carreselling.application.service.model.ColorSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.color.list.contract.ColorItem;
import br.com.carreselling.usecase.color.list.contract.ColorListResponse;
import br.com.carreselling.usecase.color.list.mapping.ColorListMapper;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/colors")
@Validated
public class ListColorEndpoint {

    private final IColorService colorService;

    private final TenantContext tenantContext;

    public ListColorEndpoint(IColorService colorService, TenantContext tenantContext) {
        this.colorService = colorService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ApiResponse<ColorListResponse> list() {
        int companyId = tenantContext.getCurrentCompanyId();
        List<ColorSummary> colors = colorService.listColors(companyId);
        List<ColorItem> items = colors.stream()
            .map(ColorListMapper::toItem)
            .toList();
        return new ApiResponse<>(new ColorListResponse(items));
    }
}
