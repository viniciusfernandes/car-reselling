package br.com.carreselling.usecase.brand.list.contract;

import java.util.List;

public record BrandListResponse(List<BrandItem> brands) {
}
