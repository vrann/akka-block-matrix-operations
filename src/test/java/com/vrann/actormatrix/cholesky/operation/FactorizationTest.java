package com.vrann.actormatrix.cholesky.operation;

import org.apache.spark.ml.linalg.DenseMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FactorizationTest {

    @Test
    public void testApply()
    {
        DenseMatrix A = new DenseMatrix(3, 3, new double[]{
                4, 12, -16,
                12, 37, -43,
                -16, -43, 98

        });
        DenseMatrix L11 = Factorization.apply(A);
        System.out.println(L11);
        DenseMatrix expected = new DenseMatrix(3, 3, new double[]{
                2.0,   6,     -8,
                12.0,  1.0,   5.0,
                -16.0, -43.0, 3.0,
        });

        System.out.println(expected);
        assertEquals(expected, L11);

        A = new DenseMatrix(9, 9, new double[]{
                4, 12, -16,      8, 22, -19,    3, 5, 11,
                12, 37, -43,     21, 13, 9,     7, 12, -4,
                -16, -43, 98,    -6, -1, 1,    32, 54, 17,

                8, 21, -6,       2, 14, 39,    87, -25, 28,
                22, 13, -1,      14, -11, 61,   14, 34, -32,
                -19, 9, 1,       39, 61, 8,     65, 55, 21,

                3, 7, 32,        87, 14, 65,    13, 26, -33,
                5, 12, 54,       -25, 34, 55,   26, -15, 41,
                11, -4, 17,      28, -32, 21,   -33, 41, 29
        });

//        A = UnformattedMatrixReader
//                .<DenseMatrix>ofFileLocator(
//                        (Position.fromCoordinates(0, 0), BlockMatrixType.aMN) -> new File(
//                                this.getClass().getResource(
//                                    String.format("/test/matrix-aMN-0-0.bin")
//                                ).getPath()
//                        ), 0, 0, BlockMatrixType.aMN)
//                .readMatrix(new DenseMatrixFactory());
//
//        System.out.println(A.toString());
//        System.out.println(Factorization.apply(A).toString());
//
//        double[] res = Factorization.apply(A).values();
//        System.out.println(res[30]);
//        System.out.println(res[31]);
//        System.out.println(res[32]);
//
//        System.out.println(res[39]);
//        System.out.println(res[40]);
//        System.out.println(res[41]);
//
//        System.out.println(res[48]);
//        System.out.println(res[49]);
//        System.out.println(res[50]);
//
//        A = new DenseMatrix(4, 4, new double[]{
//            7,  3, -1,  2,
//            3,  8,  1, -4,
//            -1,  1,  4, -1,
//            2, -4, -1,  6
//        });
//        System.out.println(Factorization.apply(A));
    }
}