package com.metao.product.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class DateFormatter {

    public final static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

    public static LocalDate now() {
        return LocalDate.now();
    }
}
