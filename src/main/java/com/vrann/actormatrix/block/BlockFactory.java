package com.vrann.actormatrix.block;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.state.StateEventHandler;
import com.vrann.actormatrix.block.state.StateManagement;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;
import com.vrann.actormatrix.cholesky.message.L21MatrixDataAvailable;

public class BlockFactory {

    private final HandlerFactory factory;

    public BlockFactory(HandlerFactory factory) {
        this.factory = factory;
    }

    public Block createBlockElement(Position pos, int sectionId)
    {
        StateEventHandler.Builder<Enum> l21EventHandlerBuilder = StateEventHandler.newBuilder(Enum.class);
        StateManagement stateMachine = StateManagement.newBuilder().addSateHandler(
                L21MatrixDataAvailable.class,
                l21EventHandlerBuilder.build()
        ).build();

        if (pos.getX() == pos.getY() && pos.getX() == 0) {
//            BlockStateMachine stateMachine = BlockStateMachine.createDiagonal(pos);
            return new DiagonalBlock(factory, stateMachine, pos, sectionId);
        } else if (pos.getX() == pos.getY()) {
//            BlockStateMachine stateMachine = BlockStateMachine.createDiagonal(pos);
            return new DiagonalBlock(factory, stateMachine, pos, sectionId);
        } else if (pos.getX() == 0) {
//            BlockStateMachine stateMachine = BlockStateMachine.createSubDiagonal(pos);
            return new SubdiagonalBlock(factory, stateMachine, pos, sectionId);
        } else {
//            BlockStateMachine stateMachine = BlockStateMachine.createSubDiagonal(pos);
            return new SubdiagonalBlock(factory, stateMachine, pos, sectionId);
        }
    }

}
