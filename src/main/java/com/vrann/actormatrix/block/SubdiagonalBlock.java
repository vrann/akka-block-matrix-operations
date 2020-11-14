package com.vrann.actormatrix.block;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.vrann.actormatrix.ActorSelfReference;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.SectionCoordinator;
import com.vrann.actormatrix.actor.BlockActor;
import com.vrann.actormatrix.block.state.BlockMatrixState;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;
import com.vrann.actormatrix.cholesky.handler.BlockMatrixDataAvailableHandler;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;
import com.vrann.actormatrix.cholesky.message.A11MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.L21MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.aMNMatrixDataAvailable;

import static com.vrann.actormatrix.cholesky.CholeskyBlockState.*;
import static com.vrann.actormatrix.cholesky.CholeskyBlockState.L21_CALCULATED;
import static com.vrann.actormatrix.cholesky.CholeskyEvent.PROCESSED;
import static com.vrann.actormatrix.cholesky.CholeskyEvent.RECEIVED;
import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.*;

public class SubdiagonalBlock implements Block {

    private final Position position;
    private HandlerFactory factory;
    private int sectionId;
    private final BlockMatrixState<CholeskyMatrixType> stateMachine;
    private final BlockMatrixState<SectionCoordinator.SectionTypes> sectionStateMachine;

    public SubdiagonalBlock(
            HandlerFactory factory,
            Position position,
            int sectionId,
            BlockMatrixState<SectionCoordinator.SectionTypes> sectionStateMachine) {
        this.factory = factory;
        this.position = position;
        this.sectionId = sectionId;
        this.stateMachine = createStateMachine();
        this.sectionStateMachine = sectionStateMachine;
        for (var topic: getSubscriptions()) {
            sectionStateMachine.expect(SectionCoordinator.SectionTypes.TOPIC, new TopicEventContext(topic));
        }
        sectionStateMachine.expect(SectionCoordinator.SectionTypes.BLOCK, position);
    }

    private BlockMatrixState<CholeskyMatrixType> createStateMachine() {
        var builder = BlockMatrixState.<CholeskyMatrixType>getBuilder();
        builder.expected(aMN, position);
        builder.when(aMN, RECEIVED).one().setState(aMN, DATA_INITIALIZED);
        for (int i = 0; i < position.getY(); i++) {
            builder.expected(L21, Position.fromCoordinates(position.getX(), i));
        }
        builder.expected(L11, Position.fromCoordinates(position.getY(), position.getY()));
        builder.when(L11, RECEIVED).one().setState(L11, L11_RECEIVED);
        builder.when(L21, RECEIVED).all().setState(L21, L21_ALL_RECEIVED);
        builder.onCondition(
                (new BlockMatrixState.Condition<>(aMN, RECEIVED)).one(),
                (event) -> {
                    System.out.printf("aMN received at positions %s\n", position);
                }
        );
        builder.onCondition(
                (new BlockMatrixState.Condition<>(L21, RECEIVED)).one(),
                (event) -> {
                    System.out.printf("l21 received at positions %s\n", position);
                }
        );
        builder.onCondition(
                (new BlockMatrixState.Condition<>(L21, RECEIVED)).one(),
                (event) -> {
                    System.out.printf("l11 received at positions %s\n", position);
                }
        );
        builder.when(L21, PROCESSED)
                .all() //.one(Position) //set(List<Positions>)
                .setState(L21, L21_CALCULATED); //run(Consumer<>)
        builder.when(L11, PROCESSED)
                .one() //.one(Position) //set(List<Positions>)
                .setState(L11, L11_CALCULATED); //run(Consumer<>)
        return builder.build();

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
    public Position getPosition() {
        return position;
    }

    @Override
    public BlockState status() {
        return stateMachine.getState();
    }

    @Override
    public AbstractActor.Receive getReceive(LoggingAdapter log, ActorSelfReference selfReference, ActorSystem system)
    {
        ReceiveBuilder builder = new ReceiveBuilder();
        return builder
                .match(A11MatrixDataAvailable.class, message -> {
                    BlockMatrixDataAvailableHandler<A11MatrixDataAvailable> handler = factory.getHandler(A11, stateMachine);
                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
                })
                .match(aMNMatrixDataAvailable.class, message -> {
                    log.info("received message with the topic {}", message.getTopic());
                    BlockMatrixDataAvailableHandler<aMNMatrixDataAvailable> handler = factory.getHandler(aMN, stateMachine);
                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
                })
                .match(L21MatrixDataAvailable.class, message -> {
                    BlockMatrixDataAvailableHandler<L21MatrixDataAvailable> handler = factory.getHandler(L21, stateMachine);
                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
                })
                .match(DistributedPubSubMediator.SubscribeAck.class,
                        message -> {
                            log.info("subscribed to topic {}", message.subscribe().topic());
                            sectionStateMachine.triggerEvent(SectionCoordinator.MessageStatus.RECEIVED, SectionCoordinator.SectionTypes.TOPIC, TopicEventContext.from(message.subscribe().topic()));
                        }
                ).matchAny(message -> {
                    log.info("SubDiagonalBlock received unknown message {}", message.getClass().getName());
                })
                .build();
    }
}
