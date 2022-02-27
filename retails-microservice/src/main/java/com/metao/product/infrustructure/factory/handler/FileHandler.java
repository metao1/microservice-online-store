package com.metao.product.infrustructure.factory.handler;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.metao.product.infrustructure.util.FileUtil;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;


@Service
public class FileHandler {

    public Stream<String> readFromFile(@NonNull String strPath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        var path = Paths.get(classLoader.getResource(strPath).getFile());
        return FileUtil.readDataFromResources(path);
    }
}
