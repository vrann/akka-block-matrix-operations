package com.vrann.actormatrix.filetransfer.message;

import akka.stream.SourceRef;
import akka.util.ByteString;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;
import com.vrann.actormatrix.Message;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;

import java.io.Serializable;

public class FileTransfer implements Serializable, Message {
    private final SourceRef<ByteString> sourceRef;
    private final String fileName;
    private final CholeskyMatrixType matrixType;
    private final Position position;

    public FileTransfer(
        String fileName,
        CholeskyMatrixType matrixType,
        Position position,
        SourceRef<ByteString> sourceRef
    ) {
        this.sourceRef = sourceRef;
        this.fileName = fileName;
        this.matrixType = matrixType;
        this.position = position;
    }

    public String getFileName() {
        return fileName;
    }

    public CholeskyMatrixType getMatrixType() {
        return matrixType;
    }

    public Position getPosition() {
        return position;
    }

    public SourceRef<ByteString> getSourceRef() {
        return sourceRef;
    }

    @Override
    public String toString() {
        return String.format("file-transfer-ready-%s-%s-%s", fileName, position, matrixType);
    }
}
