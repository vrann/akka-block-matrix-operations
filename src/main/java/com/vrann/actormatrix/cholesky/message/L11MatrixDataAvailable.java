package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;

import java.io.File;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.L11;

public class L11MatrixDataAvailable extends BlockMatrixDataAvailable {

    public L11MatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, L11, filePath, sectionId);
    }

    public static L11MatrixDataAvailable create(
            Position position, File filePath, int sectionId
    ) {
        return new L11MatrixDataAvailable(position, filePath, sectionId);
    }
}
