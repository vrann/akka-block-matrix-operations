package com.vrann.actormatrix.cholesky.operation;

import com.github.fommil.netlib.BLAS;
import org.apache.spark.ml.linalg.DenseMatrix;

public class RowBlockUpdate {

    public static DenseMatrix apply(DenseMatrix L21, DenseMatrix A22) {
        double[] L22 = A22.toArray();
        BLAS.getInstance().dsyrk("L", "N", A22.numRows(), A22.numCols(), -1, L21.values(), L21.numCols(), 1, L22, A22.numRows());
        return new DenseMatrix(A22.numRows(), A22.numCols(), L22);
    }
}
