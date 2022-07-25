package com.metao.book.shared.infra.eventlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.shared.domain.base.DomainEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The domain event log service is responsible for storing and retrieving
 * {@link DomainEvent}s. These can be used for
 * auditing or for integration with other systems / bounded contexts.
 *
 * @see StoredDomainEvent
 * @see DomainEventLog
 * @see DomainEventLogAppender
 */
@Service
public class DomainEventLogService {

    private static final int LOG_SIZE = 20;

    private final ObjectMapper objectMapper;

    DomainEventLogService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Appends the given domain event to the event log.
     *
     * @param domainEvent the domain event to append.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void append(@NonNull DomainEvent domainEvent) {
        var storedEvent = new StoredDomainEvent(domainEvent, objectMapper);
    }
}
