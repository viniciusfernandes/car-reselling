package br.com.carreselling.usecase.vehicle.list.contract;

import java.util.List;

public record VehicleListResponse(List<VehicleListItem> items, int page, int size, long total) {
}
