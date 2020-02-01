package archive.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

import java.io.Serializable;

public class A11Ready implements Message, Serializable {

    //private final DenseMatrix A11;
    private Position position;
    private BlockMatrixType matrixType;

    private A11Ready(Position position, BlockMatrixType matrixType) {
        //this.A11 = matrix;
        this.position = position;
        this.matrixType = matrixType;
    }

    public static A11Ready instance(Position position, BlockMatrixType matrixType) {
        return new A11Ready(position, matrixType);
    }

    public Position getPosition() {
        return position;
    }

    public BlockMatrixType getMatrixType() {
        return matrixType;
    }

    //public DenseMatrix getA11() {
        //return A11;
    //}
}