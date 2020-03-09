package com.vrann.actormatrix.block;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.vrann.actormatrix.ActorSelfReference;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.actor.BlockActor;
import com.vrann.actormatrix.block.state.BlockMatrixState;
import com.vrann.actormatrix.block.state.StateManagement;
import com.vrann.actormatrix.cholesky.CholeskyEvent;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;
import com.vrann.actormatrix.cholesky.handler.BlockMatrixDataAvailableHandler;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;
import com.vrann.actormatrix.cholesky.message.A11MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.BlockMatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.L21MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.aMNMatrixDataAvailable;

import java.util.Arrays;
import java.util.List;

import static com.vrann.actormatrix.cholesky.CholeskyBlockState.L11_CALCULATED;
import static com.vrann.actormatrix.cholesky.CholeskyBlockState.L11_RECEIVED;
import static com.vrann.actormatrix.cholesky.CholeskyEvent.PROCESSED;
import static com.vrann.actormatrix.cholesky.CholeskyEvent.RECEIVED;
import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.*;

public class SubdiagonalBlock implements Block {

    private final Position position;
    private HandlerFactory factory;
    private int sectionId;
    private StateManagement stateMachine;

    public SubdiagonalBlock(
            HandlerFactory factory,
            StateManagement stateMachine,
            Position position,
            int sectionId
    ) {
        this.factory = factory;
        this.position = position;
        this.sectionId = sectionId;
        this.stateMachine = stateMachine;
    }

    private BlockMatrixState<CholeskyMatrixType, CholeskyEvent> createStateMachine() {
        return BlockMatrixState
                .<CholeskyMatrixType, CholeskyEvent>expected(L11, Position.fromCoordinates(1, 1))
                .expected(L11, Position.fromCoordinates(2, 2))
                .expected(L11, Position.fromCoordinates(3, 3))
                .expected(L11, Position.fromCoordinates(0, 0))
                .when(L11, RECEIVED)
                .all() //.one(Position) //set(List<Positions>)
                .setState(L11, L11_RECEIVED) //run(Consumer<>)
                .onCondition(
                        (new BlockMatrixState.Condition<>(L11, RECEIVED)).one(),
                        (condition, state) -> {
                            System.out.println("l11 received");
                        }
                )
                .when(L11, PROCESSED)
                .all() //.one(Position) //set(List<Positions>)
                .setState(L11, L11_CALCULATED) //run(Consumer<>)
                .build();
    }

    @Override
    public String getName()
    {
        return String.format("subdiagonal-%d-%d-actor", position.getX(), position.getY());
    }

    @Override
    public Props getProps() {
        return Props.create(BlockActor.class, () -> new BlockActor(this));
    }

    @Override
    public List<String> getSubscriptions()
    {
        return Arrays.asList(
                BlockMatrixDataAvailable.generateTopic(position, A11),
                BlockMatrixDataAvailable.generateTopic(position, aMN),
                BlockMatrixDataAvailable.generateTopic(position, L21)
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
                    BlockMatrixDataAvailableHandler<A11MatrixDataAvailable> handler = factory.getHandler(A11, createStateMachine());
                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
                })
                .match(aMNMatrixDataAvailable.class, message -> {
                    BlockMatrixDataAvailableHandler<aMNMatrixDataAvailable> handler = factory.getHandler(aMN, createStateMachine());
                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
                })
                .match(L21MatrixDataAvailable.class, message -> {
                    BlockMatrixDataAvailableHandler<L21MatrixDataAvailable> handler = factory.getHandler(L21, createStateMachine());
                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
                })
                .match(DistributedPubSubMediator.SubscribeAck.class,
                        message -> log.info("subscribed to topic {}", message.subscribe().topic())
                ).matchAny(message -> {
                    log.info("SubDiagonalBlock received unknown message {}", message.getClass().getName());
                })
                .build();
    }
}
