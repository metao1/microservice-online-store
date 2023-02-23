package com.metao.book.shared.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@UtilityClass
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

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert.
     * @return the JSON byte array.
     */
    public static byte[] convertObjectToJsonBytes(ObjectMapper mapper, Object object) throws IOException {
        return mapper.writeValueAsBytes(object);
    }
}
