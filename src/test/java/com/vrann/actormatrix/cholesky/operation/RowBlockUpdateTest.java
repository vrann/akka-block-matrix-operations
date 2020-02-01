package com.vrann.actormatrix.cholesky.operation;

import org.apache.spark.ml.linalg.DenseMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RowBlockUpdateTest {

    RowBlockUpdate update = new RowBlockUpdate();
    Factorization factorization = new Factorization();

    @Test
    void apply() {
        DenseMatrix L21 = new DenseMatrix(3, 3, new double[]{
                4.0, 11.0, -9.5,
                -3.0, -53.0, 66.0,
                13.666666666666666, 117.33333333333333, -135.0
        });
        System.out.println(L21);

        DenseMatrix A22 = new DenseMatrix(3, 3, new double[]{
                2, 22, -19,
                0, 13, 9,
                0, 0, 1
        });

        System.out.println(A22);

        DenseMatrix L22 = update.apply(L21, A22);
        System.out.println(L22);
        A22 = factorization.apply(L22);

        DenseMatrix expected = new DenseMatrix(3, 3, new double[]{
                -209.77777777777777, 14.0, 39.0,
                14.0, -11.0, 61.0,
                39.0, 61.0, 8.0,
        });

        System.out.println(expected);
        assertEquals(expected, A22);
    }
}