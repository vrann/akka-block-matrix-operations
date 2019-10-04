package com.vrann.dataformat;

import org.apache.spark.ml.linalg.DenseMatrix;
import org.apache.spark.ml.linalg.Matrix;

public class DenseMatrixFactory implements MatrixFactory<DenseMatrix> {

    public DenseMatrixFactory() {}

    public DenseMatrix createMatrix(int numRows, int numCols, double[] data) {
        return new DenseMatrix(numRows, numCols, data);
    }
}
