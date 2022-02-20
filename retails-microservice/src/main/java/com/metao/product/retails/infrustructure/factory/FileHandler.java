package com.metao.product.retails.infrustructure.factory;

import com.metao.product.retails.infrustructure.util.FileUtil;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.Paths;

public class FileHandler {

    public String readFromFile(@NonNull String strPath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        var path = Paths.get(classLoader.getResource(strPath).getFile());
        return FileUtil.readDataFromResources(path);
    }
}
