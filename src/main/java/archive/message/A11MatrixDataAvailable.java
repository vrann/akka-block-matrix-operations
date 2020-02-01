package archive.message;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

import java.io.File;

public class A11MatrixDataAvailable extends BlockMatrixDataAvailable {

    public A11MatrixDataAvailable(Position position, File filePath, int sectionId) {
        super(position, BlockMatrixType.A11, filePath, sectionId);
    }
}
