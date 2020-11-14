package com.vrann.actormatrix.cholesky;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.ConfigFactory;
import com.vrann.actormatrix.*;
import com.vrann.actormatrix.block.BlockFactory;
import com.vrann.actormatrix.block.BlockMatrixType;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;
import com.vrann.actormatrix.filetransfer.message.FileTransfer;
import com.vrann.actormatrix.filetransfer.message.FileTransferReady;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.function.Function;


public class CholeskyIntegrationTest {

    static SectionCoordinator sectionCoordinator1;
    static SectionCoordinator sectionCoordinator2;
    static SectionCoordinator sectionCoordinator3;
    static ActorSystemContext context;

    @BeforeAll
    public static void setup() {
        context = ActorSystemContext.create(
                ConfigFactory.load("app.conf")
        );

        Function<Integer, FileLocator> getFileLocator = (a) ->
            (String fileName) -> {
                context.getLog().info(String.format("test-data/node%d/.actorchoreography/%s", a, fileName));
                return new File(
                        CholeskyIntegrationTest.class.getClassLoader()
                                .getResource(String.format("test-data/node%d/.actorchoreography", a))
                                .getPath() + "/" + fileName
                );
        };

        File fileSection1 = new File(CholeskyIntegrationTest.class.getClassLoader().getResource("node1.conf").getPath());
        sectionCoordinator1 = new SectionCoordinator(
                SectionConfiguration.createCustomNodeConfiguration(fileSection1),
                new BlockFactory(HandlerFactory.create(context)),
                context,
                getFileLocator.apply(1)
        );

        File fileSection2 = new File(CholeskyIntegrationTest.class.getClassLoader().getResource("node2.conf").getPath());
        sectionCoordinator2 = new SectionCoordinator(
                SectionConfiguration.createCustomNodeConfiguration(fileSection2),
                new BlockFactory(HandlerFactory.create(context)),
                context,
                getFileLocator.apply(2)
        );

        File fileSection3 = new File(CholeskyIntegrationTest.class.getClassLoader().getResource("node3.conf").getPath());
        sectionCoordinator3 = new SectionCoordinator(
                SectionConfiguration.createCustomNodeConfiguration(fileSection3),
                new BlockFactory(HandlerFactory.create(context)),
                context,
                getFileLocator.apply(3)
        );
    }

    @AfterAll
    public static void teardown() {
        //TestKit.shutdownActorSystem(context.getActorSystem());
    }

    private void sendMessage(int x, int y, int sectionId) {
        String filename = String.format("matrix-aMN-%d-%d.bin", x, y);
        //CholeskyMatrixType type = x == y ? CholeskyMatrixType.aMN : CholeskyMatrixType.aMN;
        System.out.printf("Test is sending message %s\n", FileTransferReady.getTopic(Position.fromCoordinates(x, y)));
        context.getMediator().tell(new DistributedPubSubMediator.Publish(
                FileTransferReady.getTopic(Position.fromCoordinates(x, y)),
                FileTransferReady.message(
                        Position.fromCoordinates(x, y),
                        CholeskyMatrixType.aMN, filename,
                        sectionId)
        ), ActorRef.noSender());
    }

    @Test
    public void testIt() {
        LoggingAdapter log = Logging.getLogger(context.getActorSystem(), this);
        new TestKit(context.getActorSystem()) {
            {
                sectionCoordinator1.createSectionActors();
                sectionCoordinator1.start();
                sectionCoordinator2.createSectionActors();
                sectionCoordinator2.start();
                sectionCoordinator3.createSectionActors();
                sectionCoordinator3.start();

                within(
                        Duration.ofSeconds(20),
                        () -> {
                            awaitCond(() -> sectionCoordinator1.status().equals(SectionCoordinator.SectionStatus.SUBSCRIBED));
                            awaitCond(() -> sectionCoordinator2.status().equals(SectionCoordinator.SectionStatus.SUBSCRIBED));
                            awaitCond(() -> sectionCoordinator3.status().equals(SectionCoordinator.SectionStatus.SUBSCRIBED));
                            return null;
                        }
                );
                log.info("All sections have ben started");
                sendMessage(0, 0, 2);
                sendMessage(0, 1, 3);
                sendMessage(0, 2, 3);
                sendMessage(1, 0, 3);
                sendMessage(1, 1, 2);
                sendMessage(1, 2, 1);
                sendMessage(2, 0, 1);
                sendMessage(2, 1, 1);
                sendMessage(2, 2, 2);

                //final TestKit probe = new TestKit(context.getActorSystem());
                within(
                        Duration.ofSeconds(10),
                        () -> {
                                awaitCond(() -> sectionCoordinator1.status().equals(SectionCoordinator.SectionStatus.RUNNING));
                                awaitCond(() -> sectionCoordinator2.status().equals(SectionCoordinator.SectionStatus.RUNNING));
                                awaitCond(() -> sectionCoordinator3.status().equals(SectionCoordinator.SectionStatus.RUNNING));
                                //
                                // this.expectMsgClass(Duration.ofSeconds(10), FileTransfer.class);
                                //expectNoMessage();
                                sectionCoordinator1.stop();
                                sectionCoordinator2.stop();
                                sectionCoordinator3.stop();
                                return null;
                        }
                );
            }
        };
    }
}
