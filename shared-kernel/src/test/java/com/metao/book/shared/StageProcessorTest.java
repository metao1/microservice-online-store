package com.metao.book.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.shared.application.service.StageProcessor;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StageProcessorTest {

    public static Stream<Arguments> provideArguments() {
        return Stream.of(
            Arguments.of("test", "TEST", (Function<String, String>) String::toUpperCase)
        );
    }

    public static Stream<Arguments> provideNullArgs() {
        return Stream.of(
            Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    void acceptExceptionallyWithMapIsOk(String input, String expectation, Function<String, String> function) {
        StageProcessor
            .accept(input)
            .map(function)
            .acceptExceptionally((event, exp) -> {
                    assertThat(event).isEqualTo(expectation);
                    assertThat(exp).isNull();
                }
            );
    }

    @ParameterizedTest
    @MethodSource("provideNullArgs")
    void acceptExceptionallyWhenMapWithNullInputIsFailed(String input) {
        StageProcessor
            .accept(input)
            .map(String::toUpperCase)
            .acceptExceptionally((event, exp) -> {
                    assertThat(exp).isInstanceOf(NullPointerException.class);
                    assertThat(event).isNull();
                }
            );
    }

    @ParameterizedTest
    @MethodSource("provideNullArgs")
    void applyExceptionallyWithNullInputIsFailed(String input) {
        StageProcessor
            .accept(input)
            .applyExceptionally((event, exp) -> {
                    assertThat(event).isNull();
                    assertThat(exp).isNull();
                    return null;
                }
            );
    }

    @Test
    void applyExceptionallyWithNullInputIsFailed() {

        record ProcessorInput(String input) {

        }

        record ProcessorOutput(String output) {

        }

        class Mapper {

            public ProcessorOutput convert(ProcessorInput input) {
                return new ProcessorOutput(input.input().toUpperCase());
            }
        }

        StageProcessor
            .accept(new ProcessorInput("test"))
            .map(input -> new Mapper().convert(input))
            .acceptExceptionally((output, exp) -> {
                    assertThat(exp).isNull();
                    assertThat(output).isInstanceOf(ProcessorOutput.class);
                    assertThat(output.output()).isEqualTo("TEST");
                }
            );
    }
}