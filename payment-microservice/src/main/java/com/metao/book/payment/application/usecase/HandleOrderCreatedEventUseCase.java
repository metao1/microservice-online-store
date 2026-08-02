package com.metao.book.payment.application.usecase;

import com.metao.book.shared.architecture.ApplicationUseCase;
@ApplicationUseCase
public interface HandleOrderCreatedEventUseCase {

    void handle(HandleOrderCreatedEventCommand command);
}
