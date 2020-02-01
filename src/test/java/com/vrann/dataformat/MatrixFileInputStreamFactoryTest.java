package com.vrann.dataformat;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

import java.io.DataInputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MatrixFileInputStreamFactoryTest {

    private final MatrixFileInputStreamFactory testFactory = new MatrixFileInputStreamFactory(
            (Position pos, BlockMatrixType matrixType) -> new File(this.getClass().getResource(
                    String.format("/test/test-%d-%d.bin", pos.getX(), pos.getY())).getPath())
    );

    @org.junit.jupiter.api.Test
    void createMatrixBlockInputStream() {
        try {
            DataInputStream is = testFactory.createMatrixBlockInputStream(new Position(0, 0), BlockMatrixType.L11);
            byte[] bufLengthInt = new byte[is.available()];
            is.readFully(bufLengthInt);
            assertEquals("test", new String(bufLengthInt));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail(e.getMessage());
        }
    }
}