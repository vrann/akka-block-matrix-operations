package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;

import java.io.File;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.L21;

public class L21MatrixDataAvailable extends BlockMatrixDataAvailable {

    public L21MatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, L21, filePath, sectionId);
    }

    public static L21MatrixDataAvailable create(
            Position position, File filePath, int sectionId
    ) {
        return new L21MatrixDataAvailable(position, filePath, sectionId);
    }
}
