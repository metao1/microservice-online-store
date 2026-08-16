package com.metao.book.payment.infrastructure.persistence.repository;

import com.metao.book.payment.application.port.ConsumedMessagePort;
import com.metao.book.shared.architecture.OutboundAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@OutboundAdapter
@RequiredArgsConstructor
public class ConsumedMessageRepository implements ConsumedMessagePort {

    private final SpringDataConsumedMessageRepository repository;

    @Override
    @Transactional
    public boolean claim(String consumerName, String eventId) {
        return repository.claim(consumerName, eventId) == 1;
    }
}
