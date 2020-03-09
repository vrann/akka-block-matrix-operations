package com.vrann.actormatrix.cholesky.message;

import com.vrann.actormatrix.Message;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;

public interface BlockMatrixMessage extends Message {
    Position getPosition();
    BlockMatrixType getMatrixType();
}
