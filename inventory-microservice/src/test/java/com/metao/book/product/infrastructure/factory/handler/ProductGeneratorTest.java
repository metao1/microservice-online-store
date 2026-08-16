package com.metao.book.product.infrastructure.factory.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.product.application.usecase.ProductUseCase;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

class ProductGeneratorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(GeneratorConfiguration.class);

    @TempDir
    Path temporaryDirectory;

    @Test
    void validatesReadableProductSeedResource() throws Exception {
        Path seedFile = Files.createFile(temporaryDirectory.resolve("products.txt"));
        ProductGenerator generator = productGenerator(new FileSystemResource(seedFile));

        assertThatCode(generator::validateSeedResource).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingProductSeedResource() {
        ProductGenerator generator = productGenerator(new FileSystemResource(temporaryDirectory.resolve("missing-products.txt")));

        assertThatThrownBy(generator::validateSeedResource)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Product seed resource is not readable");
    }

    @Test
    void startsGeneratorWithConfiguredReadableProductSeedResource() throws Exception {
        Path seedFile = Files.createFile(temporaryDirectory.resolve("configured-products.txt"));

        contextRunner
            .withPropertyValues("spring.profiles.active=generator", "product.seed.resource=" + seedFile.toUri())
            .run(context -> assertThat(context).hasSingleBean(ProductGenerator.class));
    }

    @Test
    void failsGeneratorStartupForMissingConfiguredProductSeedResource() {
        Path missingSeedFile = temporaryDirectory.resolve("missing-configured-products.txt");

        contextRunner
            .withPropertyValues("spring.profiles.active=generator", "product.seed.resource=" + missingSeedFile.toUri())
            .run(context -> assertThat(context).hasFailed().getFailure()
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("Product seed resource is not readable"));
    }

    @Test
    void doesNotCreateGeneratorWithoutGeneratorProfile() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(ProductGenerator.class));
    }

    private ProductGenerator productGenerator(FileSystemResource resource) {
        return new ProductGenerator(mock(ProductUseCase.class), new ObjectMapper(), resource);
    }

    @Configuration(proxyBeanMethods = false)
    @Import(ProductGenerator.class)
    static class GeneratorConfiguration {

        @Bean
        ProductUseCase productUseCase() {
            return mock(ProductUseCase.class);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
