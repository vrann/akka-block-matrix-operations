package com.vrann.actormatrix.block;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;

public class BlockFactory {

    private final HandlerFactory factory;

    public BlockFactory(HandlerFactory factory) {
        this.factory = factory;
    }

    public Block createBlockElement(Position pos, int sectionId)
    {
        if (pos.getX() == pos.getY() && pos.getX() == 0) {
            return new DiagonalBlock(factory, pos, sectionId);
        } else if (pos.getX() == pos.getY()) {
            return new DiagonalBlock(factory, pos, sectionId);
        } else if (pos.getX() == 0) {
            return new SubdiagonalBlock(factory, pos, sectionId);
        } else {
            return new SubdiagonalBlock(factory, pos, sectionId);
        }
    }

}
