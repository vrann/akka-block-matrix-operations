package archive.handler;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.BlockMatrixType;
import java.io.File;

public class MatrixTypePositionFileLocator {

    public static File getFile(Position position, BlockMatrixType blockMatrixType) {
       return new File((new StringBuilder())
                .append(System.getProperty("user.home"))
                .append("/.actorchoreography/")
                .append(
                        String.format("matrix-%s-%d-%d.bin",
                                blockMatrixType,
                                position.getX(),
                                position.getY())
                ).toString());
    }
}
