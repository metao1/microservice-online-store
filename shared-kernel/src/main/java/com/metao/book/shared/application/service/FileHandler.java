package com.metao.book.shared.application.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

@UtilityClass
public class FileHandler {

    public static Stream<String> readResourceInPath(@NonNull Class<?> clazz, @NonNull String strPath) throws IOException {
        var resource = clazz.getClassLoader().getResource(strPath);
        if (resource == null) {
            throw new IOException("Could not find the resource specified in path: " + strPath);
        }
        return Files.lines(Paths.get(resource.getPath()), StandardCharsets.UTF_8);
    }
}
