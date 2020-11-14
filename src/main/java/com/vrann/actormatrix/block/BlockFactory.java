package com.vrann.actormatrix.block;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.SectionCoordinator;
import com.vrann.actormatrix.block.state.BlockMatrixState;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;

public class BlockFactory {

    private final HandlerFactory factory;

    public BlockFactory(HandlerFactory factory) {
        this.factory = factory;
    }

    public Block createBlockElement(Position pos, int sectionId, BlockMatrixState<SectionCoordinator.SectionTypes> stateMachine)
    {

        if (pos.getX() == pos.getY() && pos.getX() == 0) {
//            BlockStateMachine stateMachine = BlockStateMachine.createDiagonal(pos);
            return new DiagonalBlock(factory, pos, sectionId, stateMachine);
        } else if (pos.getX() == pos.getY()) {
//            BlockStateMachine stateMachine = BlockStateMachine.createDiagonal(pos);
            return new DiagonalBlock(factory, pos, sectionId, stateMachine);
        } else if (pos.getX() == 0) {
//            BlockStateMachine stateMachine = BlockStateMachine.createSubDiagonal(pos);
            return new SubdiagonalBlock(factory, pos, sectionId, stateMachine);
        } else {
//            BlockStateMachine stateMachine = BlockStateMachine.createSubDiagonal(pos);
            return new SubdiagonalBlock(factory, pos, sectionId, stateMachine);
        }
    }

}
