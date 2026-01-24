package br.com.carreselling.usecase.service.list.contract;

import java.math.BigDecimal;
import java.util.List;

public record ServiceListResponse(List<ServiceItem> services, BigDecimal total) {
}
