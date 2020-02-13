package com.vrann.actormatrix.filetransfer.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.filetransfer.handler.SectionMessageHandler;
import com.vrann.actormatrix.filetransfer.message.FileTransfer;
import com.vrann.actormatrix.filetransfer.message.FileTransferReady;

import java.util.List;

public class FileTransferRequesterActor extends AbstractActor {

    private ActorSystem system;
    private ActorMaterializer materializer;
    private LoggingAdapter log;
    private ActorRef mediator;
    private int sectionId;
    //private SectionCoordinator coordinator;
    private List<Position> positions;
    private SectionMessageHandler<FileTransferReady> fileTransferReadyHandler;
    private SectionMessageHandler<FileTransfer> fileTransferHandler;

    static public Props props(
            ActorMaterializer materializer,
            ActorSystem system,
            SectionMessageHandler<FileTransferReady> fileTransferReadyHandler,
            SectionMessageHandler<FileTransfer> fileTransferHandler,
            List<Position> positions,
            //SectionCoordinator coordinator,
            int sectionId) {
        return Props.create(
                FileTransferRequesterActor.class,
                () -> new FileTransferRequesterActor(materializer, system,
                        fileTransferReadyHandler, fileTransferHandler, positions, sectionId)
        );
    }

    public FileTransferRequesterActor(ActorMaterializer materializer, ActorSystem system,
                                      SectionMessageHandler<FileTransferReady> fileTransferReadyHandler,
                                      SectionMessageHandler<FileTransfer> fileTransferHandler,
                                      List<Position> positions,
                                      //SectionCoordinator coordinator,
                                      int sectionId) {
        this.materializer = materializer;
        this.system = system;
        log  = Logging.getLogger(system, system);
        mediator = DistributedPubSub.get(system).mediator();
        this.sectionId = sectionId;
        this.positions = positions;
        //this.coordinator = coordinator;
        this.fileTransferReadyHandler = fileTransferReadyHandler;
        this.fileTransferHandler = fileTransferHandler;

        log.info("Started FileTransferRequesterActor for section {}", sectionId);

        for (Position pos: positions) {
            String topic = FileTransferReady.getTopic(pos);
            log.info("Requesting subscription to {}", topic);
            mediator.tell(new DistributedPubSubMediator.Subscribe(topic, getSelf()), getSelf());
        }
    }

    @Override
    public AbstractActor.Receive createReceive() {

        return receiveBuilder()
                .match(FileTransferReady.class, message -> {
                    log.info("Received message {}", message);
                    fileTransferReadyHandler.handle(message, sectionId, getSelf());
                }).match(FileTransfer.class, message -> {
                    log.info("Received message {}", message);
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
