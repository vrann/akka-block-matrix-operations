package com.vrann.actormatrix.filetransfer.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;
import com.vrann.actormatrix.Message;

import java.io.Serializable;

public class FileTransferReady implements Serializable, Message {

    private final String fileName;
    private final int sourceSectionId;
    private final BlockMatrixType matrixType;
    private final Position position;

    public FileTransferReady(Position position, BlockMatrixType matrixType, String fileName, int sourceSectionId) {
        this.fileName = fileName;
        this.sourceSectionId = sourceSectionId; //we need this section id in order to tell where to look for the file for; Section Manager won't help here
        this.position = position;
        this.matrixType = matrixType;
    }

    public static FileTransferReady message(Position position, BlockMatrixType matrixType, String fileName, int sourceSectionId) {
        return new FileTransferReady(position, matrixType, fileName, sourceSectionId);
    }

    public static String getTopic(Position position) {
        return String.format("data-ready-%d-%d", position.getX(), position.getY());
    }

    public String getFileName() {
        return fileName;
    }

    public BlockMatrixType getMatrixType() {
        return matrixType;
    }

    public int getSourceSectionId() {
        return sourceSectionId;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("file-transfer-ready-%s-%d-%s-%s", fileName, sourceSectionId, position, matrixType);
    }
}
