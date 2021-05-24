package com.metao.product.checkout.utils;

import java.time.LocalDate;

public class DateFormatter {

    public static long now() {
        return LocalDate.now().toEpochDay();
    }
}
