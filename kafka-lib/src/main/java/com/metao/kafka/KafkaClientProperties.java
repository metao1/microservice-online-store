package com.metao.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("spring.kafka")
public class KafkaClientProperties {

    private List<String> bootstrapServers = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();

    public Map<String, Object> buildAdminProperties() {
        Map<String, Object> adminProperties = new HashMap<>(properties);
        if (bootstrapServers != null && !bootstrapServers.isEmpty()) {
            adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        }
        return adminProperties;
    }
}
