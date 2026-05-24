package br.com.carreselling.usecase.payment.document.list.contract;

import java.util.List;

public record PaymentDocumentListResponse(List<PaymentDocumentItem> documents) {}
