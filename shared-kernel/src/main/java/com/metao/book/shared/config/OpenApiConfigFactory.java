package com.metao.book.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

/**
 * Factory for creating OpenAPI configurations with consistent metadata.
 * Eliminates duplicate OpenApiConfig classes across services.
 */
public final class OpenApiConfigFactory {

    private static final String CONTACT_NAME = "Metao";
    private static final String CONTACT_URL = "https://github.com/metao1/microservice-online-store";
    private static final String LICENSE_NAME = "MIT";
    private static final String LICENSE_URL = "https://opensource.org/licenses/MIT";
    private static final String VERSION = "1.0.0";

    private OpenApiConfigFactory() {
        // Utility class
    }

    public static OpenAPI create(String title, String description, int serverPort) {
        return new OpenAPI()
            .info(new Info()
                .title(title)
                .description(description)
                .version(VERSION)
                .contact(new Contact()
                    .name(CONTACT_NAME)
                    .url(CONTACT_URL))
                .license(new License()
                    .name(LICENSE_NAME)
                    .url(LICENSE_URL)))
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort).description("Local server")
            ));
    }
}
