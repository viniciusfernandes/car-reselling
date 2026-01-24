package br.com.carreselling.config;

import java.util.List;

public record ApiErrorResponse(List<String> errors, String traceId) {
}
