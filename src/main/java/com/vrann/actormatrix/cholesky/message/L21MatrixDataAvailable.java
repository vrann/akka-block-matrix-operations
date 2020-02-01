package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

import java.io.File;

public class L21MatrixDataAvailable extends BlockMatrixDataAvailable {

    public L21MatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, BlockMatrixType.L21, filePath, sectionId);
    }
}
