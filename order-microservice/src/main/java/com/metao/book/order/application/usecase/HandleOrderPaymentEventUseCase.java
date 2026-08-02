package com.metao.book.order.application.usecase;

import com.metao.book.shared.architecture.ApplicationUseCase;
@ApplicationUseCase
public interface HandleOrderPaymentEventUseCase {

    void handle(HandleOrderPaymentEventCommand command);
}
