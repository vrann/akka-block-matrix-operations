package com.vrann.actormatrix.filetransfer.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;

import java.io.Serializable;

public class FileTransferRequest implements Serializable {

    private final String fileName;
    private final int sourceSectionId;
    private final CholeskyMatrixType matrixType;
    private final Position position;

    public FileTransferRequest(Position position, CholeskyMatrixType matrixType, String fileName, int sourceSectionId) {
        this.fileName = fileName;
        this.sourceSectionId = sourceSectionId;
        this.position = position;
        this.matrixType = matrixType;
    }

    public String getFileName() {
        return fileName;
    }

    public CholeskyMatrixType getMatrixType() {
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
        return String.format("file-transfer-request-%s-%d-%s-%s", fileName, sourceSectionId, position, matrixType);
    }
}
