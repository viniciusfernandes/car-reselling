package br.com.carreselling.usecase.brand.list.endpoint;

import br.com.carreselling.application.service.IBrandService;
import br.com.carreselling.application.service.model.BrandSummary;
import br.com.carreselling.config.ApiResponse;
import br.com.carreselling.tenant.TenantContext;
import br.com.carreselling.usecase.brand.list.contract.BrandItem;
import br.com.carreselling.usecase.brand.list.contract.BrandListResponse;
import br.com.carreselling.usecase.brand.list.mapping.BrandListMapper;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands")
@Validated
public class ListBrandEndpoint {

    private final IBrandService brandService;

    private final TenantContext tenantContext;

    public ListBrandEndpoint(IBrandService brandService, TenantContext tenantContext) {
        this.brandService = brandService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ApiResponse<BrandListResponse> list() {
        int companyId = tenantContext.getCurrentCompanyId();
        List<BrandSummary> brands = brandService.listBrands(companyId);
        List<BrandItem> items = brands.stream()
            .map(BrandListMapper::toItem)
            .toList();
        return new ApiResponse<>(new BrandListResponse(items));
    }
}
