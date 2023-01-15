package com.metao.book.product.infrastructure.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.shared.domain.financial.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDtoMapper implements DTOMapper<String, Optional<ProductDTO>> {

    private final static BigDecimal PRICE = BigDecimal.valueOf(100);
    private final ObjectMapper mapper;

    @Override
    public Optional<ProductDTO> convertToDto(String productString) {
        final ProductDTO productDTO;
        try {
            productDTO = mapper.readValue(productString, ProductDTO.class);
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
            return Optional.empty();
        }
        if (productDTO.getPrice() == null) {
            productDTO.setPrice(PRICE);
        }
        if (productDTO.getCurrency() == null) {
            productDTO.setCurrency(Currency.EUR);
        }

        return Optional.of(productDTO);
    }

}
