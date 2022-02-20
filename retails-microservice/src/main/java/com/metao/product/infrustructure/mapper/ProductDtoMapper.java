package com.metao.product.infrustructure.mapper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.metao.product.application.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ProductDtoMapper implements DTOMapper<String, Optional<Object>> {

    private final Gson gson;

    @Override
    public Optional<Object> convertToDto(String productString) {
        final ProductDTO productDTO;
        try {
            productDTO=  gson.fromJson(productString, ProductDTO.class);;
        } catch (JsonSyntaxException ex) {
            log.error(ex.getMessage());
            return Optional.empty();
        }
        return Optional.ofNullable(productDTO);
    }

}
