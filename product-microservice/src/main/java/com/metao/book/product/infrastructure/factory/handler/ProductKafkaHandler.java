package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.event.CreateProductEvent;
import com.metao.book.shared.Currency;
import com.metao.book.shared.ProductEvent;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductKafkaHandler implements MessageHandler<CreateProductEvent> {

    private final RemoteProductService remoteProductService;

    @Override
    @Transactional("kafkaTransactionManager")
    public void onMessage(@NonNull CreateProductEvent event) {
        try {
            log.info("sending product to kafka on: {}", event.occurredOn());
            var productDto = event.productDTO();
            Optional.of(productDto)
                .map(this::mapToProductEvent)
                .ifPresent(remoteProductService::handle);
        } catch (Exception ex) {
            log.warn(ex.getMessage());
        }
    }

    ProductEvent mapToProductEvent(ProductDTO productDTO) {
        return ProductEvent.newBuilder()
            .setProductId(productDTO.getAsin())
            .setTitle(productDTO.getTitle())
            .setImageUrl(productDTO.getImageUrl())
            .setDescription(productDTO.getDescription())
            .setCurrency(mapCurrency(productDTO.getCurrency()))
            .setPrice(productDTO.getPrice().doubleValue())
            .setVolume(productDTO.getVolume().doubleValue())
            .setCreatedOn(Instant.now().toEpochMilli())
            .setCategories(mapToCategories(productDTO.getCategories()))
            .build();
    }

    private String mapToCategories(Set<CategoryDTO> categories) {
        if (!CollectionUtils.isEmpty(categories)) {
            return categories.stream().map(CategoryDTO::getCategory).collect(Collectors.joining(","));
        }
        return "";
    }

    private Currency mapCurrency(com.metao.book.shared.domain.financial.Currency currency) {
        return Currency.valueOf(currency.name().toLowerCase());
    }
}
