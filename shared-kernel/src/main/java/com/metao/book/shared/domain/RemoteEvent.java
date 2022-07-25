package com.metao.book.shared.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.infra.jackson.RawJsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import javax.persistence.Transient;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemoteEvent implements Serializable {

    private static final int DOMAIN_EVENT_JSON_MAX_LENGTH = 1024;

    private String id;

    Instant occurredOn;
    Instant createdOn;

    String domainEventClassName;
    @JsonProperty("domainEventBody")
    @JsonRawValue
    @JsonDeserialize(using = RawJsonDeserializer.class)
    String domainEventBody;
    @Transient
    Class<? extends DomainEvent> domainEventClass;

    @NonNull
    public String id() {
        if (!StringUtils.hasLength(id)) {
            throw new IllegalStateException("The StoredDomainEvent has not been saved yet");
        }
        return id;
    }

    /**
     * Creates a new {@code StoredDomainEvent}.
     *
     * @param domainEvent  the domain event to store.
     * @param objectMapper the object mapper to use to convert the domain event into JSON.
     * @throws IllegalArgumentException if the domain event cannot be converted to JSON.
     */
    public static RemoteEvent createRemoteEvent(@NonNull DomainEvent domainEvent, @NonNull ObjectMapper objectMapper) {
        Objects.requireNonNull(domainEvent, "domainEvent must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        final RemoteEvent remoteEvent;
        try {
            remoteEvent = RemoteEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .occurredOn(domainEvent.occurredOn())
                    .createdOn(Instant.now())
                    .domainEventClassName(domainEvent.getClass().getName())
                    .domainEventBody(objectMapper.writeValueAsString(domainEvent))
                    .domainEventClass(domainEvent.getClass())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return remoteEvent;
    }

    /**
     * Returns the domain event deserialized to {@link #domainEventClass()}.
     *
     * @param objectMapper the object mapper to use for parsing JSON.
     * @throws IllegalStateException if the JSON string cannot be turned into a domain event of the correct class.
     */
    @NonNull
    public DomainEvent toDomainEvent(@NonNull ObjectMapper objectMapper) {
        return toDomainEvent(objectMapper, domainEventClass());
    }

    /**
     * Returns the domain event.
     *
     * @param objectMapper     the object mapper to use for parsing JSON.
     * @param domainEventClass the class to deserialize to.
     * @throws IllegalStateException if the JSON string cannot be turned into a domain event of the correct class.
     */
    @NonNull
    public <T extends DomainEvent> T toDomainEvent(@NonNull ObjectMapper objectMapper,
                                                   @NonNull Class<T> domainEventClass) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(domainEventClass, "domainEventClass must not be null");
        try {
            return objectMapper.readValue(domainEventBody, domainEventClass);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not deserialize domain event from JSON", ex);
        }
    }

    /**
     * Returns the domain event as a JSON string.
     */
    @NonNull
    public String toJsonString() {
        return domainEventBody;
    }

    /**
     * Returns the domain event as a {@link JsonNode}.
     *
     * @param objectMapper the object mapper to use for parsing JSON.
     * @throws IllegalStateException if the JSON string cannot be parsed into a JSON node.
     */
    @NonNull
    public JsonNode toJsonNode(@NonNull ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        try {
            return objectMapper.readTree(domainEventBody);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not deserialize domain event from JSON", ex);
        }
    }

    /**
     * Returns the class of the domain event.
     *
     * @throws IllegalStateException if the class does not exist in the class path.
     */
    @NonNull
    public Class<? extends DomainEvent> domainEventClass() {
        if (domainEventClass == null) {
            domainEventClass = lookupDomainEventClass();
        }
        return domainEventClass;
    }

    /**
     * Returns the name of the class of the domain event.
     */
    @NonNull
    public String domainEventClassName() {
        return domainEventClassName;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> lookupDomainEventClass() {
        try {
            return (Class<? extends DomainEvent>) Class.forName(domainEventClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Could not load domain event class", ex);
        }
    }

    /**
     * Returns the date and time when the domain event occurred.
     */
    @NonNull
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, domainEventClass=%s]", getClass().getSimpleName(), id, domainEventClassName);
    }

}
