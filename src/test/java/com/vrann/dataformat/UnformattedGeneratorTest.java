package com.vrann.dataformat;

import com.vrann.actormatrix.cholesky.operation.Factorization;
import org.apache.spark.ml.linalg.DenseMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class UnformattedGeneratorTest {

    @Test
    void writeThenReadMatrix(@TempDir Path tempDir) throws FileNotFoundException, IOException {
        final File filePath = new File("/Users/etulika/Projects/akka-block-manager/generated.bin");

        int rowsCount = 7000;
        int colsCount = 7000;
        double[] testData = new double[49000000];
        for (int i = 0; i < 49000000; i++) {
            testData[i] = Math.random();
        }
        DenseMatrix matrix = new DenseMatrix(rowsCount, colsCount, testData);
        final UnformattedMatrixWriter<DenseMatrix> writer = UnformattedMatrixWriter.ofFile(filePath);
        writer.writeMatrix(matrix);
        final UnformattedMatrixReader<DenseMatrix> reader = UnformattedMatrixReader.ofFile(filePath);
        DenseMatrix matrix1 = reader.readMatrix(DenseMatrix::new);
        //System.out.println(matrix1);
        long beforeStart = System.nanoTime();
        DenseMatrix L11 = Factorization.apply(matrix1);
        long timePassed = System.nanoTime() - beforeStart;
        System.out.println("time passed: " + TimeUnit.NANOSECONDS.toMillis(timePassed) + "milliseconds");
        System.out.println(L11);
        System.out.println(filePath);
        final var writerL1 = UnformattedMatrixWriter.ofFile(new File("/Users/etulika/Projects/akka-block-manager/L1-of-generated.bin"));
        writerL1.writeMatrix(L11);

        //10000 x 10000
        //time passed: 1907 milliseconds 1.907
        //763M
//
//        real	0m3.895s
//        user	0m2.947s
//        sys	0m1.165s => 4.112

        //1000 x 1000
        //time passed: 121 milliseconds 0.121
        //7.6 Mb
//
//        real	0m0.239s
//        user	0m0.045s
//        sys	0m0.019s => 0.064

        //5000 x 5000
        //time passed: 420 milliseconds 0.420
        //191Mb
//        real	0m1.071s
//        user	0m0.729s
//        sys	0m0.316s => 1.045

        //7000x7000
        //time passed: 895 milliseconds 0.859
        //374 MB
//        real	0m1.981s
//        user	0m1.435s
//        sys	0m0.557s => 1.992


        //100 x 100
        //time passed: 136 milliseconds 0.136
        //78K
//        real	0m0.161s
//        user	0m0.015s
//        sys	0m0.000s => 0.015s
    }
}