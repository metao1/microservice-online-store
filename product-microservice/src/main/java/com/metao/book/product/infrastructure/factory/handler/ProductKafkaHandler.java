package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.domain.event.CreateProductEvent;
import com.metao.book.shared.Currency;
import com.metao.book.shared.ProductEvent;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductKafkaHandler implements MessageHandler<CreateProductEvent> {

    private final RemoteProductService remoteProductService;

    @Override
    public void onMessage(@NonNull CreateProductEvent event) {
        log.info("sending product to kafka on timestamp: {}", event.occurredOn());
        Optional.of(event)
            .map(this::mapToProductEvent)
            .ifPresent(remoteProductService::sendToKafka);
    }

    ProductEvent mapToProductEvent(CreateProductEvent createProductEvent) {
        var productDto = createProductEvent.productDto();
        return ProductEvent.newBuilder()
            .setProductId(createProductEvent.id())
            .setTitle(productDto.title())
            .setImageUrl(productDto.imageUrl())
            .setDescription(productDto.description())
            .setCurrency(mapCurrency(productDto.currency()))
            .setPrice(productDto.price().doubleValue())
            .setVolume(productDto.volume().doubleValue())
            .setCreatedOn(createProductEvent.occurredOn().toEpochMilli())
            .setCategories(mapToCategories(productDto.categories()))
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
