package com.metao.book.product.infrastructure.factory.handler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.product.application.usecase.ProductUseCase;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;

class ProductGeneratorTest {

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

    private ProductGenerator productGenerator(FileSystemResource resource) {
        return new ProductGenerator(mock(ProductUseCase.class), new ObjectMapper(), resource);
    }
}
