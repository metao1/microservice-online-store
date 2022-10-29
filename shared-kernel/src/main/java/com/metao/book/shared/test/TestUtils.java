package com.metao.book.shared.test;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestUtils {

    public static class StreamBuilder {

        public static <R> Stream<R> of(
            Class<R> clazz,
            int low,
            int range,
            Function<? super Integer, ? extends R> mapper
        ) {
            return IntStream.range(low, range)
                .boxed()
                .map(mapper)
                .map(clazz::cast);
        }
    }
}
