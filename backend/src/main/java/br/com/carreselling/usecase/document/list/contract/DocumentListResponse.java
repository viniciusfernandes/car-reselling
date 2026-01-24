package br.com.carreselling.usecase.document.list.contract;

import java.util.List;

public record DocumentListResponse(List<DocumentItem> documents) {
}
