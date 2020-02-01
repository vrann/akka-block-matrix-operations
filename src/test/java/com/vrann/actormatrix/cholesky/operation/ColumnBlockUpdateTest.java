package com.vrann.actormatrix.cholesky.operation;

import org.apache.spark.ml.linalg.DenseMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColumnBlockUpdateTest {

    ColumnBlockUpdate update = new ColumnBlockUpdate();

    @Test
    void apply() {
        DenseMatrix L11 = new DenseMatrix(3, 3, new double[]{
                2.0, 6.0, -8.0,
                0.0, 1.0,  5.0,
                0.0, 0.0,  3.0
        });
        System.out.println(L11);

        DenseMatrix A21 = new DenseMatrix(3, 3, new double[]{
                8.0, 22.0, -19.0,
                21.0, 13.0, 9.0,
                -6.0, -1.0, 1.0
        });

        DenseMatrix L21 = update.apply(L11, A21);
        DenseMatrix expected = new DenseMatrix(3, 3, new double[]{
                4.0, 11.0, -9.5,
                -3.0, -53.0, 66.0,
                13.666666666666666, 117.33333333333333, -135.0
        });
        assertEquals(expected, L21);
    }
}