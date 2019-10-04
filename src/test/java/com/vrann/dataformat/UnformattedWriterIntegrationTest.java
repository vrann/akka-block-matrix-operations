package com.vrann.dataformat;

import org.apache.spark.ml.linalg.DenseMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UnformattedWriterIntegrationTest {

    private final double[] testData = new double[]{12.0, 20.0, 13.0, 20.0, 14.0, 40.0, 50.0, 6.0};
    private final int rowsCount = 2;
    private final int colsCount = 4;

    @Test
    void writeThenReadMatrix(@TempDir Path tempDir) throws FileNotFoundException, IOException {
        final File filePath = new File(tempDir.resolve("test-out.bin").toString());

        DenseMatrix matrix = new DenseMatrix(rowsCount, colsCount, testData);
        final UnformattedMatrixWriter<DenseMatrix> writer = UnformattedMatrixWriter.ofFile(filePath);
        final UnformattedMatrixReader<DenseMatrix> reader = UnformattedMatrixReader.ofFile(filePath);
        writer.writeMatrix(matrix);

        try {
            assertEquals(
                    new DenseMatrix(rowsCount, colsCount, testData),
                    reader.readMatrix(DenseMatrix::new));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            fail();
        }
    }
}