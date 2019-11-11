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
        log.info("Started FileTransferRequesterActor for section {}", sectionId);

        for (Position pos: coordinator.positions()) {
            String topic = FileTransferReady.getTopic(pos);
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
                    fileTransferReadyHandler.handle(message, sectionId, getSelf());
                }
        ).match(
                FileTransfer.class, message -> {
                    System.out.printf("Received message FileTransfer (%d, %d) %s %s\n",
                            message.getPosition().getX(), message.getPosition().getY(),
                            message.getMatrixType(), message.getFileName());
                    fileTransferHandler.handle(message, sectionId, getSelf());
                }).match(DistributedPubSubMediator.SubscribeAck.class,
                    message -> log.info("subscribed to topic {}", message.subscribe().topic())
                ).matchAny(message -> {
                    log.info("FileTransferRequesterActor received unknown message {}", message.getClass().getName());
                    getSender().tell("Unsupported", getSelf());
                })
                .build();
    }
}
