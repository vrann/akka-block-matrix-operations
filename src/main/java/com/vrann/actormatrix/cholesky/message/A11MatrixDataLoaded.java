package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

import java.io.File;

public class A11MatrixDataLoaded extends BlockMatrixDataLoaded {

    private final BlockMatrixType type = BlockMatrixType.A11;

    public A11MatrixDataLoaded(Position position, File filePath, int sectionId) {
        super(position, BlockMatrixType.A11, filePath, sectionId);
    }

    public static String generateTopic(Position pos) {
        return String.format("section-data-loaded-%s-%d-%d",
                BlockMatrixType.A11,
                pos.getX(),
                pos.getY());
    }
}
