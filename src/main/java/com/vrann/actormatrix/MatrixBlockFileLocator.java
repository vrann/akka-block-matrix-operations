package com.vrann.actormatrix;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MatrixBlockFileLocator implements FileLocator {

    public Path getMatrixBlockFilePath(String fileName) {
        StringBuilder pathBuilder = (new StringBuilder())
                .append(System.getProperty("user.home"))
                .append("/.actorchoreography/")
                .append(fileName);
        return Paths.get(pathBuilder.toString());
    }
}
