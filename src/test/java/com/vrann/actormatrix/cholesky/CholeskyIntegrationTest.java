package com.vrann.actormatrix.cholesky;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.testkit.javadsl.TestKit;
import akka.util.ByteString;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigSyntax;
import com.vrann.actormatrix.*;
import com.vrann.actormatrix.block.BlockFactory;
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

//        StringBuilder pathBuilder = (new StringBuilder())
//                .append(System.getProperty("user.home"))
//                .append("/.actorchoreography/node.conf");
//        File file = new File(pathBuilder.toString());

        FileLocator fileLocator1 = (fileName) -> {
            context.getLog().info(String.format("test-data/node%d/.actorchoreography/%s", 1, fileName));
            return new File(
                    CholeskyIntegrationTest.class.getClassLoader()
                            .getResource(String.format("test-data/node%d/.actorchoreography", 1))
                            .getPath() + "/" + fileName
            );
        };

        FileLocator fileLocator2 = (fileName) -> {
            context.getLog().info(String.format("test-data/node%d/.actorchoreography/%s", 2, fileName));
            return new File(
                    CholeskyIntegrationTest.class.getClassLoader()
                            .getResource(String.format("test-data/node%d/.actorchoreography", 2))
                            .getPath() + "/" + fileName
            );
        };

        FileLocator fileLocator3 = (fileName) -> {
            context.getLog().info(String.format("test-data/node%d/.actorchoreography/%s", 3, fileName));
            return new File(
                    CholeskyIntegrationTest.class.getClassLoader()
                            .getResource(String.format("test-data/node%d/.actorchoreography", 3))
                            .getPath() + "/" + fileName
            );
        };

        File fileSection1 = new File(CholeskyIntegrationTest.class.getClassLoader().getResource("node1.conf").getPath());
        sectionCoordinator1 = new SectionCoordinator(
                SectionConfiguration.createCustomNodeConfiguration(fileSection1),
                new BlockFactory(HandlerFactory.create(context)),
                context,
                fileLocator1
        );

        File fileSection2 = new File(CholeskyIntegrationTest.class.getClassLoader().getResource("node2.conf").getPath());
        sectionCoordinator2 = new SectionCoordinator(
                SectionConfiguration.createCustomNodeConfiguration(fileSection2),
                new BlockFactory(HandlerFactory.create(context)),
                context,
                fileLocator2
        );

        File fileSection3 = new File(CholeskyIntegrationTest.class.getClassLoader().getResource("node3.conf").getPath());
        sectionCoordinator3 = new SectionCoordinator(
                SectionConfiguration.createCustomNodeConfiguration(fileSection3),
                new BlockFactory(HandlerFactory.create(context)),
                context,
                fileLocator3
        );
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(context.getActorSystem());
    }

    private void sendMessage(int x, int y, int sectionId) {
        String filename = String.format("matrix-aMN-%d-%d.bin", x, y);
        context.getMediator().tell(new DistributedPubSubMediator.Publish(
                FileTransferReady.getTopic(Position.fromCoordinates(x, y)),
                FileTransferReady.message(
                        Position.fromCoordinates(x, y),
                        BlockMatrixType.aMN, filename,
                        sectionId)
        ), ActorRef.noSender());
    }

    @Test
    public void testIt() {
        new TestKit(context.getActorSystem()) {
            {
                sectionCoordinator1.createSectionActors();
                sectionCoordinator1.start();
                sectionCoordinator2.createSectionActors();
                sectionCoordinator2.start();
                sectionCoordinator3.createSectionActors();
                sectionCoordinator3.start();

                sendMessage(0, 0, 2);
                sendMessage(0, 1, 3);
                sendMessage(0, 2, 3);
                sendMessage(1, 0, 3);
                sendMessage(1, 1, 2);
                sendMessage(1, 2, 1);
                sendMessage(2, 0, 1);
                sendMessage(2, 1, 1);
                sendMessage(2, 2, 2);

                /*final TestKit probe = new TestKit(context.getActorSystem());
                within(
                        Duration.ofSeconds(3),
                        () -> {
                            try {
                                //awaitCond(probe::msgAvailable);
                                probe.expectMsgClass(Duration.ofSeconds(5), FileTransfer.class);
                                //expectNoMessage();
                                return null;
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            return null;
                        }
                );*/
            }
        };
    }
}
