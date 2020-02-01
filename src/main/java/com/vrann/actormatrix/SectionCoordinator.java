package com.vrann.actormatrix;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.LoggingAdapter;
import akka.management.javadsl.AkkaManagement;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.actor.BlockActor;
import com.vrann.actormatrix.filetransfer.actor.FileTransferRequesterActor;
import com.vrann.actormatrix.filetransfer.actor.FileTransferSenderActor;
import com.vrann.actormatrix.block.Block;
import com.vrann.actormatrix.block.BlockFactory;
import com.vrann.actormatrix.filetransfer.handler.FileTransferHandler;
import com.vrann.actormatrix.filetransfer.handler.FileTransferReadyHandler;
import com.vrann.actormatrix.filetransfer.handler.FileTransferRequestHandler;

import java.util.HashMap;
import java.util.List;

public class SectionCoordinator {

    private final List<Position> sectionPositions;
    private ActorSystem actorSystem;
    private LoggingAdapter log;
    private final BlockFactory elementFactory;
    private final int sectionId;
    private ActorMaterializer materializer;
    private final HashMap<Position, Block> blocks = new HashMap<>();
    private ActorRef mediator;
    private final FileLocator fileLocator;

    public SectionCoordinator(
            SectionConfiguration configuration,
            BlockFactory elementFactory,
            ActorSystemContext context,
            FileLocator fileLocator
    ) {
        this.sectionPositions = configuration.getSectionBlockPositions();
        this.fileLocator = fileLocator;
        this.elementFactory = elementFactory;
        this.sectionId = configuration.getSectionId();

        actorSystem = context.getActorSystem();
        log =  context.getLog();
        materializer = context.getMaterializer();
        mediator = context.getMediator();
    }

    private String getSectionSystemName() {
        return String.format("actor-system-section-%s", sectionId);
    }

    public void stop() {
        log.info("Shutting down actor system");
        actorSystem.terminate();
        log.info("Actor system terminated");
    }

    public void start() {
        log.info("Starting actor system %s", getSectionSystemName());
        AkkaManagement.get(actorSystem).start();
        log.info("Actor system started");
    }

    public void createSectionActors()
    {
        log.info("Number of actors in section: {}", sectionPositions.size());

        for (Position pos: sectionPositions) {
            log.info("Starting actor for {}", pos);
            Block element = elementFactory.createBlockElement(pos, sectionId);
            blocks.put(pos, element);
            ActorRef block = actorSystem.actorOf(BlockActor.props(element), element.getName());
        }


        actorSystem.actorOf(FileTransferRequesterActor.props(
                materializer,
                actorSystem,
                new FileTransferReadyHandler(
                    log,
                    mediator,
                    fileLocator
                ),
                new FileTransferHandler.Builder()
                        .setFileLocator(fileLocator)
                        .setLoger(log)
                        .setMaterializer(materializer)
                        .setMediator(mediator)
                        .build(),
                sectionPositions,
                sectionId
        ), String.format("FileTransferRequesterActor-%d", sectionId));

        actorSystem.actorOf(FileTransferSenderActor.props(
                materializer,
                actorSystem,
                new FileTransferRequestHandler(
                    log,
                    materializer,
                    actorSystem.dispatcher(),
                    fileLocator
                ),
                sectionId
        ), String.format("FileTransferSenderActor-%d", sectionId));
    }
}
