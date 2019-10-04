package com.vrann.actormatrix.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.message.Message;
import com.vrann.blockedcholesky.operation.BlockMatrixType;

import java.io.Serializable;

public class FileTransferRequest implements Serializable, Message {

    private final String fileName;
    private final int sourceSectionId;
    private final BlockMatrixType matrixType;
    private final Position position;

    public FileTransferRequest(Position position, BlockMatrixType matrixType, String fileName, int sourceSectionId) {
        this.fileName = fileName;
        this.sourceSectionId = sourceSectionId;
        this.position = position;
        this.matrixType = matrixType;
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

}
