package com.vrann.actormatrix;

import java.io.File;
import java.nio.file.Path;

@FunctionalInterface
public interface FileLocator {
    File getMatrixBlockFilePath(String filename);
}
