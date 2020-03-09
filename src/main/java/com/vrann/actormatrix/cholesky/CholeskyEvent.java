package com.vrann.actormatrix.cholesky;

import com.vrann.actormatrix.block.state.BlockMatrixStateEvent;

public enum CholeskyEvent implements BlockMatrixStateEvent {
    RECEIVED,
    PROCESSED
}
