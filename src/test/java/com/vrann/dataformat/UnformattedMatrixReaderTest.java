package com.vrann.dataformat;

import org.apache.spark.ml.linalg.DenseMatrix;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class UnformattedMatrixReaderTest {

    private final double[] testData = new double[]{12.0, 20.0, 13.0, 20.0, 14.0, 40.0, 50.0, 6.0};
    private final int rowsCount = 2;
    private final int colsCount = 4;

    @org.junit.jupiter.api.Test
    void readMatrix() {
        try {
            assertEquals(
                    new DenseMatrix(rowsCount, colsCount, testData),
                    UnformattedMatrixReader
                            .<DenseMatrix>ofStream(createDataInputStream(testData))
                            .readMatrix(new DenseMatrixFactory()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    //use data generator
    private DataInputStream createDataInputStream(double[] data) {
        byte[] byteArray = new byte[data.length * 8 + 2 * 4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(rowsCount);
        byteBuffer.putInt(colsCount);
        for (double value: data) {
            byteBuffer.putDouble(value);
        }
        InputStream input = new ByteArrayInputStream(byteArray);
        return new DataInputStream(input);
    }
}