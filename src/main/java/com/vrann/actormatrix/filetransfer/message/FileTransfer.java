package com.vrann.actormatrix.filetransfer.message;

import akka.stream.SourceRef;
import akka.util.ByteString;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;
import archive.message.Message;

import java.io.Serializable;

public class FileTransfer implements Serializable, Message {
    private final SourceRef<ByteString> sourceRef;
    private final String fileName;
    private final BlockMatrixType matrixType;
    private final Position position;

    public FileTransfer(
        String fileName,
        BlockMatrixType matrixType,
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

    public BlockMatrixType getMatrixType() {
        return matrixType;
    }

    public Position getPosition() {
        return position;
    }

    public SourceRef<ByteString> getSourceRef() {
        return sourceRef;
    }
}
