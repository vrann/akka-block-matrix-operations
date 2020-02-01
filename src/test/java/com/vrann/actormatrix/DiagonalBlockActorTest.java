package com.vrann.actormatrix;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.pubsub.DistributedPubSub;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.testkit.javadsl.TestKit;
import akka.util.ByteString;
import com.typesafe.config.ConfigFactory;
import com.vrann.actormatrix.block.BlockFactory;
import com.vrann.actormatrix.cholesky.CholeskyIntegrationTest;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;
import com.vrann.actormatrix.filetransfer.actor.FileTransferRequesterActor;
import com.vrann.actormatrix.filetransfer.actor.FileTransferSenderActor;
import com.vrann.actormatrix.filetransfer.handler.FileTransferReadyHandler;
import com.vrann.actormatrix.filetransfer.handler.FileTransferRequestHandler;
import com.vrann.actormatrix.filetransfer.message.FileTransfer;
import com.vrann.actormatrix.filetransfer.message.FileTransferReady;
import com.vrann.actormatrix.cholesky.BlockMatrixType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;


class DiagonalBlockActorTest {

    static ActorSystem system;
    static LoggingAdapter log;
    static ActorSystemContext context;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("l11-actor-system", ConfigFactory.load("app.conf"));
        context = ActorSystemContext.createFromActorSystem(system);
        log = context.getLog();
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testIt() {
        new TestKit(system) {
            {
                final ActorMaterializer materializer = ActorMaterializer.create(system);
                final List<Position> positions = Arrays.asList(
                        Position.fromCoordinates(0,0),
                        Position.fromCoordinates(0,1)
                );

                final ActorRef mediator = DistributedPubSub.get(system).mediator();
                File file = new File(CholeskyIntegrationTest.class.getResource("node.conf").getPath());
                final SectionCoordinator sc = new SectionCoordinator(
                        SectionConfiguration.createCustomNodeConfiguration(file),
                        new BlockFactory(HandlerFactory.create(context)), context,
                        new MatrixBlockFileLocator()
                        );

                final TestKit probe = new TestKit(system);

                final ActorRef requester = system.actorOf(FileTransferRequesterActor.props(materializer, system,
                        //(FileTransferReady message, int sectionId) -> {},
                        (FileTransferReady message, int sectionId, ActorRef self) -> {
                            new FileTransferReadyHandler(
                                    log, mediator,
                                    (fileName) -> new File(
                                            this.getClass().getResource(String.format("/test/%s", fileName)).getPath())
                            ).handle(message, sectionId, self);
                        },
                        (FileTransfer message, int sectionId, ActorRef self) -> {
                            Sink<ByteString, CompletionStage<ByteString>> concatSink =
                                    Sink.fold(ByteString.fromString(""), (akk, entry) -> {akk = akk.concat(entry);
                                        System.out.printf("akk.utf8String() %s \n", akk.utf8String());
                                        probe.getRef().tell(akk.utf8String(), getRef());
                                        return akk;});

                            message.getSourceRef().getSource().limit(100).to(concatSink).run(materializer);
                        }, positions, 1), "FileTransferRequesterActor");

                final ActorRef sender =  system.actorOf(
                        FileTransferSenderActor.props(materializer, system,
                                (message, ref, self) -> new FileTransferRequestHandler(
                                        log,
                                        materializer,
                                        system.dispatcher(),
                                        (fileName) -> new File(
                                                this.getClass().getResource(String.format("/test/%s", fileName))
                                                        .getPath())
                                ).handle(message, probe.getRef(), self),
                                2), "FileTransferSenderActor");

                requester.tell(new FileTransferReady(Position.fromCoordinates(0, 0), BlockMatrixType.aMN, "data-0-0.dat", 2), probe.getRef());

                within(
                        Duration.ofSeconds(3),
                        () -> {
                            try
                            {
                                awaitCond(probe::msgAvailable);
                                probe.expectMsgClass(Duration.ZERO, FileTransfer.class);
                                expectNoMessage();
                                return null;
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            return null;
                        });
            }
        };
    }

}