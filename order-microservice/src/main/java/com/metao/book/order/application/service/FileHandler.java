package com.metao.book.order.application.service;

import com.metao.book.order.application.util.FileUtil;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileHandler {

    public Stream<String> readFromFile(@NonNull String strPath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        var path = Paths.get(classLoader.getResource(strPath).getFile());
        return FileUtil.readDataFromResources(path);
    }
}
