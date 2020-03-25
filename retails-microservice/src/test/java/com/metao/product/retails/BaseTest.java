package com.metao.product.retails;

import java.time.LocalDate;
import java.util.UUID;

public class BaseTest {

    protected static final String USER_ID = UUID.randomUUID().toString();

    protected LocalDate NOW() {
        return LocalDate.now();
    }
}
