package com.vrann.actormatrix.cholesky.operation;

import com.github.fommil.netlib.BLAS;
import org.apache.spark.ml.linalg.DenseMatrix;

public class ColumnBlockUpdate {

    public static DenseMatrix apply(DenseMatrix L11, DenseMatrix A21) {
        //intW info = new intW(0);
        double[] L21 = A21.toArray();
        BLAS.getInstance().dtrsm("R", "L", "T", "N", L11.numRows(), L11.numCols(), 1.0, L11.toArray(), L11.numCols(), L21, A21.numRows());
        return new DenseMatrix(A21.numRows(), A21.numCols(), L21);
    }
}
