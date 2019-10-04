package com.vrann.dataformat;

import org.apache.spark.ml.linalg.Matrix;

public interface MatrixFactory<T extends  Matrix> {

    T createMatrix(int numRows, int numCols, double[] data);
}
