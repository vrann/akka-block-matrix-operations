package com.vrann.blockedcholesky.operation;

import org.apache.spark.ml.linalg.DenseMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistributedFactorizationTest {
    Factorization factorization = new Factorization();
    ColumnBlockUpdate columnUpdate = new ColumnBlockUpdate();
    RowBlockUpdate rowBlockUpdate = new RowBlockUpdate();

    @Test
    public void testApply() {
        DenseMatrix A = new DenseMatrix(4, 4, new double[]{
                7, 3, -1, 2,
                3, 8, 1, -4,
                -1, 1, 4, -1,
                2, -4, -1, 6
        });

        double[] AFactorized = Factorization.apply(A).values();
        DenseMatrix expectedL11 = new DenseMatrix(2, 2, new double[]{
                AFactorized[0], AFactorized[1],
                AFactorized[4], AFactorized[5],
        });

        DenseMatrix expectedL21 = new DenseMatrix(2, 2, new double[]{
                AFactorized[2], AFactorized[3],
                AFactorized[6], AFactorized[7],
        });

        DenseMatrix expectedA22 = new DenseMatrix(2, 2, new double[]{
                AFactorized[10], AFactorized[11],
                AFactorized[14], AFactorized[15],
        });


        DenseMatrix A11 = new DenseMatrix(2, 2, new double[]{
                7, 3,
                3, 8
        });
        DenseMatrix L11 = Factorization.apply(A11);
        assertEquals(expectedL11, L11);


        DenseMatrix A21 = new DenseMatrix(2, 2, new double[]{
                -1, 2,
                1, -4
        });
        DenseMatrix L21 = columnUpdate.apply(L11, A21);
        assertEquals(expectedL21, L21);

        DenseMatrix A22 = new DenseMatrix(2, 2, new double[]{
                4, -1,
                -1, 6
        });
        DenseMatrix L22 = rowBlockUpdate.apply(L21, A22);
        A22 = factorization.apply(L22);

        assertEquals(expectedA22, A22);

    }
}