package br.com.carreselling.usecase.payment.list.mapping;

import br.com.carreselling.application.service.model.PaymentSummary;
import br.com.carreselling.usecase.payment.list.contract.PaymentItem;

public final class PaymentListMapper {

    private PaymentListMapper() {}

    public static PaymentItem toItem(PaymentSummary summary) {
        return new PaymentItem(
            summary.id(),
            summary.paymentType(),
            summary.description(),
            summary.amount(),
            summary.paymentDate(),
            summary.vehicleId(),
            summary.vehicleLicensePlate(),
            summary.referenceYear(),
            summary.referenceMonth(),
            summary.notes(),
            summary.createdAt(),
            summary.updatedAt()
        );
    }
}
