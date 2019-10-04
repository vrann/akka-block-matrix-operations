package com.vrann.dataformat;

import com.vrann.actormatrix.Position;
import com.vrann.blockedcholesky.operation.BlockMatrixType;

import java.io.File;

//file naming convention strategy
//most likely would rely on convention though should allow to extend/change it in futire
@FunctionalInterface
public interface FileLocator {
    File getFile(Position position, BlockMatrixType blockMatrixType);
}
