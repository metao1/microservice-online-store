package com.metao.book.shared.application.service;

import com.metao.book.shared.application.service.util.FileUtil;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

@UtilityClass
public class FileHandler {

    public static Stream<String> readFromFile(@NonNull Class<?> clazz, @NonNull String strPath) throws IOException {
        ClassLoader classLoader = clazz.getClassLoader();
        var path = Paths.get(classLoader.getResource(strPath).getFile());
        return FileUtil.readDataFromResources(path);
    }
}
