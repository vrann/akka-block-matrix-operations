package archive.message;

import org.apache.spark.ml.linalg.DenseMatrix;

import java.io.Serializable;

public class L11Ready implements Serializable {

    private final DenseMatrix L11;

    public L11Ready(DenseMatrix matrix) {
        this.L11 = matrix;
    }

    public DenseMatrix getL11() {
        return L11;
    }
}