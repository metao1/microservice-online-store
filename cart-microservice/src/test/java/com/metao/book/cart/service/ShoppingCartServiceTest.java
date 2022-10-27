package com.metao.book.cart.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(classes = {ShoppingCartCartFactory.class})
class ShoppingCartServiceTest {


}