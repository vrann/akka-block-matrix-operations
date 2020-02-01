package com.vrann.actormatrix.block;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.vrann.actormatrix.ActorSelfReference;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.actor.BlockActor;
import com.vrann.actormatrix.cholesky.handler.BlockMatrixDataLoadedHandler;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;
import com.vrann.actormatrix.cholesky.message.*;
import com.vrann.actormatrix.cholesky.BlockMatrixType;
import com.vrann.actormatrix.cholesky.handler.BlockMatrixDataAvailableHandler;

import java.util.Arrays;
import java.util.List;

public class DiagonalBlock implements Block {

    private final Position position;
    private HandlerFactory factory;
    private int sectionId;

    public DiagonalBlock(
            HandlerFactory factory,
            Position position,
            int sectionId
    ) {
        this.factory = factory;
        this.position = position;
        this.sectionId = sectionId;
    }

    @Override
    public String getName()
    {
        return String.format("diagonal-%d-%d-actor", position.getX(), position.getY());
    }

    @Override
    public Props getProps() {
        return Props.create(BlockActor.class, () -> new BlockActor(this));
    }

    @Override
    public List<String> getSubscriptions()
    {
        return Arrays.asList(
                A11MatrixDataLoaded.generateTopic(position)
        );
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public AbstractActor.Receive getReceive(LoggingAdapter log, ActorSelfReference selfReference, ActorSystem system)
    {
        ReceiveBuilder builder = new ReceiveBuilder();
        return builder
                .match(A11MatrixDataAvailable.class, message -> {
                    BlockMatrixDataAvailableHandler<BlockMatrixDataAvailable> handler = factory.getHandler(BlockMatrixType.A11);
                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
                })
                .match(L21MatrixDataAvailable.class, message -> {
                    BlockMatrixDataAvailableHandler<L21MatrixDataAvailable> handler = factory.getHandler(BlockMatrixType.L21);
                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
                })
                .build();
    }
}
