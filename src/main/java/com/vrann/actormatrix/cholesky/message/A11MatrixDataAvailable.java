package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;

import java.io.File;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.A11;

public class A11MatrixDataAvailable extends BlockMatrixDataAvailable {

    public A11MatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, A11, filePath, sectionId);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
