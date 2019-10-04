package com.vrann.actormatrix.message;

import org.apache.spark.ml.linalg.DenseMatrix;

import java.io.Serializable;

public class L21Ready implements Serializable {

    private final DenseMatrix L21;

    public L21Ready(DenseMatrix matrix) {
        this.L21 = matrix;
    }

    public DenseMatrix getL21() {
        return L21;
    }
}