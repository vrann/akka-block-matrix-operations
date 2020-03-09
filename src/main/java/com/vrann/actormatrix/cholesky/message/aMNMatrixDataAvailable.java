package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;

import java.io.File;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.aMN;

public class aMNMatrixDataAvailable extends BlockMatrixDataAvailable {

    public aMNMatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, aMN, filePath, sectionId);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
