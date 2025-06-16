package com.metao.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.acl.AclOperation;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component("kafkaTopics")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopicHealthIndicator extends AbstractHealthIndicator {
    private final KafkaProperties kafkaProperties;
    private final ApplicationEventPublisher eventPublisher;
    private AdminClient adminClient;

    @PostConstruct
    public void init() {
        final Properties adminProperties = new Properties();
        adminProperties.putAll(kafkaProperties.buildAdminProperties(null));
        adminClient = AdminClient.create(adminProperties);
    }

    @PreDestroy
    public void tearDown() {
        if (Objects.nonNull(adminClient)) {
            adminClient.close();
        }
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        builder.up();
        final Set<String> knownTopics = knownTopics(builder);
        final Map<String, TopicDescription> topicDescriptions = getTopicDescriptions(builder, knownTopics);
        final Set<String> missingWriteAcl = missingWriteAcl(topicDescriptions);
        if (!missingWriteAcl.isEmpty()) {
            log.error("Missing write acls for topic(s): {}", missingWriteAcl);
            builder.withDetail("missingWriteAcl", missingWriteAcl).down();
            AvailabilityChangeEvent.publish(this.eventPublisher, builder.build().getDetails(), LivenessState.BROKEN);
        }
    }

    private Set<String> missingWriteAcl(final Map<String, TopicDescription> topicDescriptions) {
        return topicDescriptions
            .entrySet()
            .stream()
            .filter(topicDescription -> !hasWriteAcl(topicDescription.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private boolean hasWriteAcl(TopicDescription topicDescription) {
        return topicDescription != null
            && topicDescription.authorizedOperations() != null
            && (topicDescription.authorizedOperations().contains(AclOperation.WRITE) || topicDescription.authorizedOperations().contains(AclOperation.ALL));
    }

    private Map<String, TopicDescription> getTopicDescriptions(final Health.Builder builder, final Collection<String> topics) {
        final DescribeTopicsOptions describeTopicsOptions = new DescribeTopicsOptions();
        describeTopicsOptions.includeAuthorizedOperations(true);
        final Map<String, TopicDescription> result = new HashMap<>();
        try {
            result.putAll(adminClient.describeTopics(topics, describeTopicsOptions).allTopicNames().get());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Could not execute check", e);
            builder.withDetail("failedCheck", e).down();
        }
        return result;
    }

    private Set<String> knownTopics(final Health.Builder builder) {
        final ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
        listTopicsOptions.listInternal(false);
        final Set<String> knownTopics = new HashSet<>();
        try {
            knownTopics.addAll(adminClient.listTopics(listTopicsOptions).names().get());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Could not execute check", e);
            builder.withDetail("failedCheck", e).down();
        }
        return knownTopics;
    }

}
