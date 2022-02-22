package com.metao.product.infrustructure.mapper;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.product.application.dto.ProductDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProductDtoMapper implements DTOMapper<String, Optional<ProductDTO>> {

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
        return Optional.ofNullable(productDTO);
    }

}
