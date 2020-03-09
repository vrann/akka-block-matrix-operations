package com.vrann.actormatrix.cholesky;

import com.vrann.actormatrix.block.BlockState;

public enum CholeskyBlockState implements BlockState {
    INIT,
    A11_RECEIVED,
    A11_CALCULATED,
    L11_CALCULATED,
    L11_RECEIVED,
    L21_CALCULATED,
    L21_ALL_RECEIVED,
    A22_CALCULATED,
    COMPLETE
}
