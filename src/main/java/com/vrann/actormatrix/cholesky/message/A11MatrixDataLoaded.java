package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;

import java.io.File;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.A11;

public class A11MatrixDataLoaded extends BlockMatrixDataLoaded {

    private final BlockMatrixType type = A11;

    public A11MatrixDataLoaded(Position position, File filePath, int sectionId) {
        super(position, A11, filePath, sectionId);
    }

    public static String generateTopic(Position pos) {
        return String.format("section-data-loaded-%s-%d-%d",
                A11,
                pos.getX(),
                pos.getY());
    }
}
