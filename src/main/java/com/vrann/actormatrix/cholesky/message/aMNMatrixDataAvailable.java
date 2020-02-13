package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

import java.io.File;

public class aMNMatrixDataAvailable extends BlockMatrixDataAvailable {

    public aMNMatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, BlockMatrixType.aMN, filePath, sectionId);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
