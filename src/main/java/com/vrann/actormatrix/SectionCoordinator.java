package com.vrann.actormatrix;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.management.javadsl.AkkaManagement;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.actor.BlockActor;
import com.vrann.actormatrix.block.*;
import com.vrann.actormatrix.block.state.BlockMatrixState;
import com.vrann.actormatrix.block.state.BlockMatrixStateEvent;
import com.vrann.actormatrix.cholesky.CholeskyBlockState;
import com.vrann.actormatrix.cholesky.CholeskyEvent;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;
import com.vrann.actormatrix.filetransfer.actor.FileTransferRequesterActor;
import com.vrann.actormatrix.filetransfer.actor.FileTransferSenderActor;
import com.vrann.actormatrix.filetransfer.handler.FileTransferHandler;
import com.vrann.actormatrix.filetransfer.handler.FileTransferReadyHandler;
import com.vrann.actormatrix.filetransfer.handler.FileTransferRequestHandler;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final BlockMatrixState<SectionTypes> stateMachine;

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
        //log =  context.getLog();
        log = Logging.getLogger(actorSystem, this);
        materializer = context.getMaterializer();
        mediator = context.getMediator();
        stateMachine = createStateMachine();
        createBlocks();
    }

    private void createBlocks()
    {
        for (Position pos: sectionPositions) {
            log.info("Starting actor for {}", pos);
            Block block = elementFactory.createBlockElement(pos, sectionId, stateMachine);
            //ActorRef ref = block.startActor(actorSystem);
            blocks.put(pos, block);
        }
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
        log.info("Starting actor system {}", getSectionSystemName());
        AkkaManagement.get(actorSystem).start();
        log.info("Actor system started");
    }

    public enum MessageStatus implements BlockMatrixStateEvent {
        RECEIVED
    }

    public enum SectionStatus implements BlockState {
        INIT,
        SUBSCRIBED,
        RUNNING,
        COMPLETED
    }

    public enum SectionTypes implements BlockMatrixType {
        TOPIC,
        BLOCK,
        SECTION
    }

    /**
     * @TODO refactor mess with the inheritance from the BLOCK statuses, states, events, etc
     * @return
     */
    private BlockMatrixState<SectionTypes> createStateMachine() {
        var builder = BlockMatrixState.<SectionTypes>getBuilder();

        builder.onCondition(
                (new BlockMatrixState.Condition<>(SectionTypes.TOPIC, MessageStatus.RECEIVED)).each(),
                (event) -> {
                    log.info("topic {} successfully subscribed", event);
                    System.out.println("l11 received");
                }
        );

        builder.when(SectionTypes.TOPIC, MessageStatus.RECEIVED).all()
                .setState(SectionTypes.SECTION, SectionStatus.SUBSCRIBED);

        builder.when(SectionTypes.BLOCK, BlockStateDefault.SUBSCRIBED).all()
                .setState(SectionTypes.SECTION, SectionStatus.SUBSCRIBED);


        builder.when(SectionTypes.BLOCK, BlockStateDefault.INIT).all()
                .setState(SectionTypes.SECTION, SectionStatus.INIT);

        builder.when(SectionTypes.BLOCK, BlockStateDefault.COMPLETE).all()
                .setState(SectionTypes.SECTION, SectionStatus.SUBSCRIBED);

        return builder.build();
    }

    public SectionStatus status() {
        var status = SectionStatus.RUNNING;
        boolean allCompleted = true;
        boolean allInit = true;
        for (Map.Entry<Position, Block> entry: blocks.entrySet()) {
            log.info("Block at position {} has current status {}", entry.getKey(), entry.getValue().status());
            if (!entry.getValue().status().equals(BlockStateDefault.INIT)) {
                allInit = false;
            }
            if (!entry.getValue().status().equals(BlockStateDefault.COMPLETE)) {
                allCompleted = false;
            }
            if (!allInit && !allCompleted) {
                break;
            }
        }
        if (allInit) {
            status = SectionStatus.INIT;
        } else if (allCompleted) {
            status = SectionStatus.COMPLETED;
        }
        log.info("Section {} has current status {}", sectionId, status);
        return status;
    }

    public void createSectionActors()
    {
        log.info("Number of actors in section: {}", sectionPositions.size());

        for (Block block: blocks.values()) {
            ActorRef ref = block.startActor(actorSystem);
        }

//        for (Position pos: sectionPositions) {
//            log.info("Starting actor for {}", pos);
//
//            Block block = elementFactory.createBlockElement(pos, sectionId, stateMachine);
//            ActorRef ref = block.startActor(actorSystem);
////            for (block.getSubscriptions()) {
////
////            }
////            blocks.put(pos, block);
////            actorSystem.actorOf(BlockActor.props(block), block.getName());
//        }


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
