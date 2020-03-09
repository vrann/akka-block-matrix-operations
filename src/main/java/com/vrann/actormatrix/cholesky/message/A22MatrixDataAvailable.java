package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;

import java.io.File;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.L11;

public class A22MatrixDataAvailable extends BlockMatrixDataAvailable {

    public A22MatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, L11, filePath, sectionId);
    }
}
