package com.vrann.actormatrix;

import java.nio.file.Path;

public interface FileLocator {
    Path getMatrixBlockFilePath(String filename);
}
