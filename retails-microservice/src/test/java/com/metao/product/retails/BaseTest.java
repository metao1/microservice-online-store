package com.metao.product.retails;

import java.util.Date;
import java.util.UUID;

public class BaseTest {

    protected static final String USER_ID = UUID.randomUUID().toString();

    protected Date NOW() {
        return new Date();
    }
}
