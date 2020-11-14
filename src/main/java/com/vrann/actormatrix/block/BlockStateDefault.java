package com.vrann.actormatrix.block;

import com.vrann.actormatrix.block.state.BlockMatrixStateEvent;

public enum BlockStateDefault implements BlockState, BlockMatrixStateEvent {
    INIT,
    COMPLETE,
    SUBSCRIBED
}
