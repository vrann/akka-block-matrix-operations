package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

import java.io.File;

public class L11MatrixDataAvailable extends BlockMatrixDataAvailable {

    public L11MatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, BlockMatrixType.L11, filePath, sectionId);
    }
}
