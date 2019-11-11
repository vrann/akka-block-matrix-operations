package com.vrann.actormatrix;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MatrixBlockFileLocator implements FileLocator {

    public File getMatrixBlockFilePath(String fileName) {
        StringBuilder pathBuilder = (new StringBuilder())
                .append(System.getProperty("user.home"))
                .append("/.actorchoreography/")
                .append(fileName);
        return new File(pathBuilder.toString());
    }
}
