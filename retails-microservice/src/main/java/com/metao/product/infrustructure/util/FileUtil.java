package com.metao.product.infrustructure.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtil {

    public static String readDataFromResources(Path path) throws IOException {
        try (Stream<String> content = Files.lines(path)) {
            return content.collect(Collectors.joining("!"));
        }
    }
}
