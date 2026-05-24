package br.com.carreselling.usecase.payment.list.contract;

import java.util.List;

public record PaymentListResponse(List<PaymentItem> payments) {}
