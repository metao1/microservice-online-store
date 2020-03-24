package com.metao.product;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    public final static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

    protected Date NOW() {
        return new Date();
    }
}
