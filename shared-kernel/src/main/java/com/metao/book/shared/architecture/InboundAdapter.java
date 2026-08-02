package com.metao.book.shared.architecture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks an HTTP, messaging, CLI, or scheduled entry point into an application. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InboundAdapter {

    Kind value();

    enum Kind {
        HTTP,
        MESSAGING
    }
}
