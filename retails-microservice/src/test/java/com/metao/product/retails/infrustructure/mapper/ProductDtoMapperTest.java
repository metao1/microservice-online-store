package com.metao.product.retails.infrustructure.mapper;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoMapperTest {

    @Test
    void givenString_convertToDto_thenIsOk() {
        var productDtoMapper = new ProductDtoMapper(new Gson());
        var productDto = productDtoMapper.convertToDto("{\"asin\": \"0439886341\", \"description\": \"Digital Organizer and Messenger\", \"title\": \"Digital Organizer and Messenger\", \"related\": {\"also_viewed\": [\"0545016266\", \"B009ECM8QY\", \"B001CODA9G\", \"B000N2X2GO\", \"B00005T3UK\", \"B00009MVFL\", \"B000VU0CQG\", \"B0000796XL\"]}, \"price\": 8.15, \"salesRank\": {\"Electronics\": 144944}, \"imageUrl\": \"http://ecx.images-amazon.com/images/I/51k0qa8fNML._SX300_.jpg\", \"categories\": [\"Electronics\", \"Computers & Accessories\", \"PDAs, Handhelds & Accessories\", \"PDAs & Handhelds\"]}");
        assertTrue(productDto.isPresent());
    }
}
