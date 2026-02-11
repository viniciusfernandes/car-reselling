package br.com.carreselling.usecase.brand.model.list.contract;

import java.util.List;

public record VehicleModelListResponse(List<VehicleModelItem> models) {
}
