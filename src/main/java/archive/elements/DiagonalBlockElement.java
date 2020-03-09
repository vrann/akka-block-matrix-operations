//package archive.elements;
//
//import akka.actor.AbstractActor;
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import akka.event.LoggingAdapter;
//import akka.japi.pf.ReceiveBuilder;
//import akka.stream.ActorMaterializer;
//import com.vrann.actormatrix.*;
//import com.vrann.actormatrix.actor.BlockActor;
//import com.vrann.actormatrix.block.Block;
//import archive.handler.*;
//import archive.message.*;
//import com.vrann.actormatrix.block.BlockMatrixType;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class DiagonalBlockElement implements Block {
//
//    private final Position position;
//    private final ActorRef mediator;
//    //private final A11ReadyHander a11Handler;
//    //private final L21ReadyHandler l21Handler;
//    private HandlerFactory factory;
//    private int sectionId;
//
//    public DiagonalBlockElement(
//            HandlerFactory factory,
//            Position position,
//            ActorRef mediator,
//            int sectionId
//            //A11ReadyHander a11Handler
//            //,
//            //L21ReadyHandler l21Handler
//    ) {
//        this.factory = factory;
//        this.position = position;
//        this.mediator = mediator;
//        //this.a11Handler = a11Handler;
//        this.sectionId = sectionId;
//        //this.l21Handler = l21Handler;
//    }
//
//    @Override
//    public String getName()
//    {
//        return String.format("diagonal-%d-%d-actor", position.getX(), position.getY());
//    }
//
//    @Override
//    public Props getProps() {
//        return Props.create(BlockActor.class, () -> new BlockActor(this));
//    }
//
//    @Override
//    public List<String> getSubscriptions()
//    {
//        return Arrays.asList(
//                BlockMatrixDataAvailable.generateTopic(position, BlockMatrixType.A11),
//                //BlockMatrixDataAvailable.generateTopic(position, BlockMatrixType.aMN),
//                //BlockMatrixDataAvailable.generateTopic(position, BlockMatrixType.A22),
//                BlockMatrixDataAvailable.generateTopic(position, BlockMatrixType.L21)
//        );
//    }
//
//    @Override
//    public Position getPosition() {
//        return position;
//    }
//
//    @Override
//    public AbstractActor.Receive getReceive(LoggingAdapter log, ActorSelfReference selfReference, ActorSystem system)
//    {
//
//        //if ()
//
////        whenState(DiagonalBlockElementState.A11Received).whenPosition(position).whenReceived().then(
////                (message) -> A11ReadyHander.handle(message, position, selfReference.getSelfInstance(), log, mediator)).then(
////            changeState(DiagonalBlockElementState.L21Received)).then(send())
////        );
//
//        ReceiveBuilder builder = new ReceiveBuilder();
//        final ActorMaterializer materializer = ActorMaterializer.create(system);
//        return builder
//                .match(A11MatrixDataAvailable.class, message -> {
//                    BlockMatrixDataAvailableHandler<A11MatrixDataAvailable> handler = factory.getHandler(BlockMatrixType.A11);
//                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
//                })
////                .match(L11MatrixDataAvailable.class, message -> {
////                    BlockMatrixDataAvailableHandler<L11MatrixDataAvailable> handler = factory.getHandler(BlockMatrixType.L11);
////                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
////                })
//                .match(L21MatrixDataAvailable.class, message -> {
//                    BlockMatrixDataAvailableHandler<L21MatrixDataAvailable> handler = factory.getHandler(BlockMatrixType.L21);
//                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
//                })
////                .match(A22MatrixDataAvailable.class, message -> {
////                    BlockMatrixDataAvailableHandler<A22MatrixDataAvailable> handler = factory.getHandler(BlockMatrixType.A22);
////                    handler.handle(message, position, sectionId, selfReference.getSelfInstance());
////                })
//                .build();
//                //means ready for factorization
//               /* .match(A11MatrixDataAvailable.class,
//                    message -> a11Handler.handle(message, position, selfReference.getSelfInstance())
//
////                    message -> {
////                        if (this.state.contains(new BlockMatrixReceivedState(DiagonalBlockElementState.A11Received, position))) {
////
////                        }
////                        if (message.getX() == position.getX() && message.getY() == position.getY()) {
////                            A11ReadyHander.handle(message, position, selfReference.getSelfInstance(), log, mediator, materializer);
////                        }
//                ).match(L21Ready.class,
//                    //prerequisite -- A22 ready was already received
//                    message -> L21ReadyHandler.handle(message, position, selfReference.getSelfInstance(), log, mediator, materializer)
////                ).match(FileTransferReady.class,
////                    message -> FileTransferReadyHandler.handle(message, position, selfReference.getSelfInstance(), log, mediator, materializer)
////                ).match(FileTransfer.class, message -> {
////                    FileTransferHandler.handle(message, position, selfReference.getSelfInstance(), log, mediator, materializer);
////                    switch (message.matrixType) {
////                        case A11:
////                            //should only accept if current section "owns" it
////                            //should file transfer be per section then? Why do we need it in block
////                            //block receives ready message and should be able to read file and construct matrix
////                            this.state.add(new BlockMatrixReceivedState(DiagonalBlockElementState.A11Received, message.position));
////                            mediator.tell(A11Ready.instance(message.position.getX(), message.position.getY()), selfReference.getSelfInstance());
////                            //we can pass section id by section coordinator knows it anyway by position
////                            // does ready means that correct section received it
////
////                            //based on matrix type it should know what message to send
////                            //matrix type and message types belongs to Cholesky domain (doesn't really makes sense in other matrices)
////                            break;
////                        case L21:
////                            this.state.add(new BlockMatrixReceivedState(DiagonalBlockElementState.L21Received, message.position));
////                            break;
////                        case A22:
////                            this.state.add(new BlockMatrixReceivedState(DiagonalBlockElementState.A22Received, message.position));
////                            break;
////                        case L11:
////                            this.state.add(new BlockMatrixReceivedState(DiagonalBlockElementState.L11Received, message.position));
////                            break;
////                    }
////                }
//                ).match(DistributedPubSubMediator.SubscribeAck.class,
//                    message -> log.info("subscribed")
//                ).matchAny(message -> log.info("received unknown message {}", message.getClass().getName()))
//                .build();*/
//    }
//}
