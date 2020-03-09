package com.vrann.actormatrix.cholesky;

import com.vrann.actormatrix.block.BlockMatrixType;

public enum CholeskyMatrixType implements BlockMatrixType {
    aMN,
    A11,
    A22,
    L11,
    L21
}
