package com.vrann.actormatrix.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.MatrixBlockFileLocator;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.SectionCoordinator;
import com.vrann.actormatrix.handler.SectionMessageHandler;
import com.vrann.actormatrix.message.FileTransfer;
import com.vrann.actormatrix.message.FileTransferReady;
import com.vrann.actormatrix.handler.FileTransferHandler;
import com.vrann.actormatrix.handler.FileTransferReadyHandler;
import com.vrann.actormatrix.handler.MessageHandler;

public class FileTransferRequesterActor extends AbstractActor {

    private ActorSystem system;
    private ActorMaterializer materializer;
    private LoggingAdapter log;
    private ActorRef mediator;
    private int sectionId;
    private SectionCoordinator coordinator;
    private SectionMessageHandler<FileTransferReady> fileTransferReadyHandler;
    private SectionMessageHandler<FileTransfer> fileTransferHandler;

    static public Props props(
            ActorMaterializer materializer,
            ActorSystem system,
            SectionMessageHandler<FileTransferReady> fileTransferReadyHandler,
            SectionMessageHandler<FileTransfer> fileTransferHandler,
            SectionCoordinator coordinator,
            int sectionId) {
        return Props.create(
                FileTransferRequesterActor.class,
                () -> new FileTransferRequesterActor(materializer, system,
                        fileTransferReadyHandler, fileTransferHandler, coordinator, sectionId)
        );
    }

    public FileTransferRequesterActor(ActorMaterializer materializer, ActorSystem system,
                                      SectionMessageHandler<FileTransferReady> fileTransferReadyHandler,
                                      SectionMessageHandler<FileTransfer> fileTransferHandler,
                                      SectionCoordinator coordinator, int sectionId) {
        this.materializer = materializer;
        this.system = system;
        log  = Logging.getLogger(system, system);
        mediator = DistributedPubSub.get(system).mediator();
        this.sectionId = sectionId;
        this.coordinator = coordinator;
        this.fileTransferReadyHandler = fileTransferReadyHandler;
        this.fileTransferHandler = fileTransferHandler;

        log.info("Started FileTransferRequesterActor for section {}", sectionId);

        for (Position pos: coordinator.positions()) {
            //this should be more specific, requesting the matrix for position
            String topic = String.format("file-transfer-ready-%d-%d", pos.getX(), pos.getY());
            log.info("Requesting subscription to {}", topic);
            log.info("Requesting subscription to {}", topic);
            mediator.tell(new DistributedPubSubMediator.Subscribe(topic, getSelf()), getSelf());
        }
    }

    @Override
    public AbstractActor.Receive createReceive() {
        System.out.println(getSelf().path());
        return receiveBuilder().match(
                FileTransferReady.class,
                message -> {
                    System.out.printf("Received message FileTransferReady (%d, %d) %s %s\n",
                            message.getPosition().getX(), message.getPosition().getY(),
                            message.getMatrixType(), message.getFileName());
                    fileTransferReadyHandler.handle(message, sectionId);
                }
        ).match(
                FileTransfer.class, message -> {
                    System.out.printf("Received message FileTransfer (%d, %d) %s %s\n",
                            message.getPosition().getX(), message.getPosition().getY(),
                            message.getMatrixType(), message.getFileName());

                    FileTransferHandler fileTransferHandler = new FileTransferHandler.Builder()
                            .setFileLocator(new MatrixBlockFileLocator())
                            .setLoger(log)
                            .setMaterializer(materializer)
                            .setMediator(mediator)
                            .setSelfReference(getSelf())
                            .build();

                    fileTransferHandler.handle(message, sectionId);
                    //}
                    //if file is not expected
//                    switch (message.matrixType) {
//                        case A11:
//                            //should only accept if current section "owns" it
//                            //should file transfer be per section then? Why do we need it in block
//                            //block receives ready message and should be able to read file and construct matrix
//                            this.state.add(new BlockMatrixReceivedState(DiagonalBlockElementState.A11Received, message.position));
//                            mediator.tell(A11Ready.instance(message.position.getX(), message.position.getY()), selfReference.getSelfInstance());
//                            //we can pass section id by section coordinator knows it anyway by position
//                            // does ready means that correct section received it
//
//                            //based on matrix type it should know what message to send
//                            //matrix type and message types belongs to Cholesky domain (doesn't really makes sense in other matrices)
//                            break;
//                        case L21:
//                            this.state.add(new BlockMatrixReceivedState(DiagonalBlockElementState.L21Received, message.position));
//                            break;
//                        case A22:
//                            this.state.add(new BlockMatrixReceivedState(DiagonalBlockElementState.A22Received, message.position));
//                            break;
//                        case L11:
//                            this.state.add(new BlockMatrixReceivedState(DiagonalBlockElementState.L11Received, message.position));
//                            break;
//                    }
                }).match(DistributedPubSubMediator.SubscribeAck.class,
                    message -> log.info("subscribed to topic {}", message.subscribe().topic())
                ).matchAny(message -> {
                    log.info("FileTransferRequesterActor received unknown message {}", message.getClass().getName());
                    getSender().tell("Unsupported", getSelf());
                })
                .build();
    }
}
