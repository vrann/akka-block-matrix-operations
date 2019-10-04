package com.vrann.actormatrix;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.vrann.actormatrix.actor.BlockActor;
import com.vrann.actormatrix.actor.FileTransferRequesterActor;
import com.vrann.actormatrix.actor.FileTransferSenderActor;
import com.vrann.actormatrix.elements.BlockElement;
import com.vrann.actormatrix.elements.BlockElementFactory;
import com.vrann.actormatrix.handler.FileTransferHandler;
import com.vrann.actormatrix.handler.FileTransferReadyHandler;
import com.vrann.actormatrix.handler.FileTransferRequestHandler;
import com.vrann.actormatrix.message.FileTransfer;
import com.vrann.actormatrix.message.FileTransferReady;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class SectionCoordinator {

    private final List<Position> sectionPositions;
    private final ActorSystem actorSystem;
    private final LoggingAdapter log;
    private final BlockElementFactory elementFactory;
    private final int sectionId;
    private final ActorMaterializer materializer;
    private final HashMap<Position, BlockElement> blocks = new HashMap<>();
    private final ActorRef mediator;

    public SectionCoordinator(
            ActorSystem system,
            List<Position> positions,
            BlockElementFactory elementFactory,
            ActorRef mediator,
            ActorMaterializer materializer,
            int sectionId
    ) {
        this.sectionPositions = positions;
        this.actorSystem = system;
        log = Logging.getLogger(actorSystem, this);
        this.elementFactory = elementFactory;
        this.sectionId = sectionId;
        this.materializer = materializer;
        this.mediator = mediator;
    }

    public void startActors()
    {
        for (Position pos: sectionPositions) {
            log.info("Starting actor for {}", pos);
            BlockElement element = elementFactory.createBlockElement(pos);
            blocks.put(pos, element);
            ActorRef block = actorSystem.actorOf(BlockActor.props(element), element.getName());
        }

        FileLocator fileLocator = new MatrixBlockFileLocator();
        actorSystem.actorOf(FileTransferRequesterActor.props(
                materializer,
                actorSystem,
                new FileTransferReadyHandler(
                    log,
                    mediator
                ),
                new FileTransferHandler.Builder()
                        .setFileLocator(fileLocator)
                        .setLoger(log)
                        .setMaterializer(materializer)
                        .setMediator(mediator)
                        .build(),
                this,
                sectionId
        ), "FileTransfer");

        actorSystem.actorOf(FileTransferSenderActor.props(
                materializer,
                actorSystem,
                new FileTransferRequestHandler(
                    log,
                    mediator,
                    materializer,
                    actorSystem.dispatcher(),
                    fileLocator
                ),
                sectionId
        ), "FileTransfer");
    }

    public List<Position> positions() {
        return sectionPositions;
    }

    public int getSectionId() {
        return sectionId;
    }

    public BlockElement getBlockElement(Position position) {
        return blocks.get(position);
    }
}
