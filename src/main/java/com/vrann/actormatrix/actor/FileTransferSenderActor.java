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
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.handler.MessageHandler;
import com.vrann.actormatrix.message.FileTransferRequest;
import com.vrann.actormatrix.handler.FileTransferRequestHandler;

public class FileTransferSenderActor extends AbstractActor {

    private static ActorSystem system;

    static public Props props(
            ActorMaterializer materializer,
            ActorSystem system,
            MessageHandler<FileTransferRequest> handler,
            int sectionId
    ) {
        return Props.create(
                FileTransferSenderActor.class,
                () -> new FileTransferSenderActor(materializer, system, handler, sectionId)
        );
    }

    private ActorMaterializer materializer;
    private LoggingAdapter log;
    ActorRef mediator;
    private int sectionId;
    private MessageHandler<FileTransferRequest> handler;

    public FileTransferSenderActor(ActorMaterializer materializer, ActorSystem system,
                                   MessageHandler<FileTransferRequest> handler,
                                   int sectionId) {
        this.materializer = materializer;
        this.system = system;
        log  = Logging.getLogger(system, system);
        mediator = DistributedPubSub.get(system).mediator();
        this.sectionId = sectionId;
        log.info("Subscribed request-file-transfer-{}", sectionId);
        log.info("Subscribed request-file-transfer-{}", sectionId);
        this.handler = handler;

        log.info("Started FileTransferRequesterActor for section {}", sectionId);

        //this should be more specific, requesting the matrix for position
        mediator.tell(new DistributedPubSubMediator.Subscribe(String.format("request-file-transfer-%d", sectionId),
                getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                    FileTransferRequest.class,
                    message -> handler.handle(message, sender(), self()))
                .match(
                    DistributedPubSubMediator.SubscribeAck.class,
                    message -> log.info("subscribed to topic {}", message.subscribe().topic()))
                .matchAny(
                    message -> log.info("FileTransferSenderActor received unknown message {}",
                            message.getClass().getName()))
                .build();
    }
}