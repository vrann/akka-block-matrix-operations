package com.vrann.actormatrix.message;

import com.vrann.actormatrix.Position;
import com.vrann.blockedcholesky.operation.BlockMatrixType;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;

public class BlockMatrixDataAvailable implements Serializable, Message {

    private final File filePath;
    private final int sectionId;
    private final BlockMatrixType matrixType;
    private final Position position;

    public BlockMatrixDataAvailable(Position position, BlockMatrixType matrixType, File filePath, int sectionId) {
        this.filePath = filePath;
        this.sectionId = sectionId; //we need this section id in order to tell where to look for the file for; Section Manager won't help here
        this.position = position;
        this.matrixType = matrixType;
    }

    public static class Builder {

        private static File filePath;
        private static int sectionId;
        private static BlockMatrixType matrixType;
        private static Position position;

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder setBlockMatrixType(BlockMatrixType type) {
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

        public BlockMatrixDataAvailable build() {
            return new BlockMatrixDataAvailable(position, matrixType, filePath, sectionId);
        }
    }

    public static BlockMatrixDataAvailable create(
            Position position, BlockMatrixType matrixType, File filePath, int sectionId
    ) {
        return new BlockMatrixDataAvailable(position, matrixType, filePath, sectionId);
    }

    public String getTopic() {
        return generateTopic(position, matrixType);
    }

    public static String generateTopic(Position pos, BlockMatrixType matrixType) {
        return String.format("section-data-available-%s-%d-%d",
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
