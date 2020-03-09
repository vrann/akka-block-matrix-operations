package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Message;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;

import java.io.File;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.A11;

public class BlockMatrixDataLoaded implements Message {

    private final File filePath;
    private final int sectionId;
    private final CholeskyMatrixType matrixType;
    private final Position position;

    public BlockMatrixDataLoaded(Position position, CholeskyMatrixType matrixType, File filePath, int sectionId) {
        this.filePath = filePath;
        this.sectionId = sectionId; //we need this section id in order to tell where to look for the file for; Section Manager won't help here
        this.position = position;
        this.matrixType = matrixType;
    }

    public static class Builder {

        private static File filePath;
        private static int sectionId;
        private static CholeskyMatrixType matrixType;
        private static Position position;

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder setBlockMatrixType(CholeskyMatrixType type) {
            this.matrixType = type;
            return this;
        }

        public Builder setFilePath(File filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder setSectionId(int sectionId) {
            this.sectionId = sectionId;
            return this;
        }

        public BlockMatrixDataLoaded build() {
            switch (matrixType) {
                case A11: return new A11MatrixDataLoaded(position, filePath, sectionId);
            }
            return new BlockMatrixDataLoaded(position, matrixType, filePath, sectionId);
        }
    }

    public static BlockMatrixDataLoaded create(
            Position position, CholeskyMatrixType matrixType, File filePath, int sectionId
    ) {
        return new BlockMatrixDataLoaded(position, matrixType, filePath, sectionId);
    }

    public String getTopic() {
        return generateTopic(position, matrixType);
    }

    public static String generateTopic(Position pos, BlockMatrixType matrixType) {
        return String.format("section-data-loaded-%s-%d-%d",
                matrixType,
                pos.getX(),
                pos.getY());
    }

    public File getFilePath() {
        return filePath;
    }

    public BlockMatrixType getMatrixType() {
        return matrixType;
    }

    public int getSectionId() {
        return sectionId;
    }

    public Position getPosition() {
        return position;
    }
}
