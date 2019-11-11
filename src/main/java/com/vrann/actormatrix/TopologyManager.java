package com.vrann.actormatrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import akka.NotUsed;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.management.javadsl.AkkaManagement;
import com.typesafe.config.*;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;

import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.ConfigFactory;
import com.vrann.actormatrix.elements.BlockElementFactory;
import com.vrann.actormatrix.message.FileTransferReady;
import com.vrann.blockedcholesky.operation.BlockMatrixType;

public class TopologyManager {

    private static ActorSystem system;
    static private LoggingAdapter log;

    public static void main(String[] args) throws IOException {
        system = ActorSystem.create("l11-actor-system", ConfigFactory.load("app1.conf"));
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        log = Logging.getLogger(system, system);
        ActorRef mediator = DistributedPubSub.get(system).mediator();


        AkkaManagement.get(system).start();
        Runtime.getRuntime().addShutdownHook(new ProcessorHook(system));

        StringBuilder pathBuilder = (new StringBuilder())
            .append(System.getProperty("user.home"))
            .append("/.actorchoreography/node.conf");

        Config config = ConfigFactory.parseFile(new File(pathBuilder.toString()),
                ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF));

        List<? extends ConfigObject> positionsArray = config.getObjectList("actors.matrix-blocks");
        List<Position> positions = new ArrayList<>();
        int sectionId = config.getInt("actors.section");

        for (ConfigObject position: positionsArray) {
            positions.add(new Position(position.get("x").render(), position.get("y").render()));
        }

        log.info("Number of actors in section: {}", positions.size());


        SectionCoordinator sc = new SectionCoordinator(
                system,
                positions,
                new BlockElementFactory(
                        log,
                        mediator,
                        sectionId
                ),
                mediator,
                materializer,
                sectionId);
        sc.startActors();

        TopologyManager app = new TopologyManager();


        final Http http = Http.get(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute(system, materializer, sectionId).flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("0.0.0.0", 2000), materializer);

    }



    private Route createRoute(ActorSystem system, ActorMaterializer materializer, int sectionId) {
        ActorRef mediator = DistributedPubSub.get(system).mediator();
        return //concat(
            path("start", () ->
                get(() -> {
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(0, 0)), FileTransferReady.message(Position.fromCoordinates(0, 0), BlockMatrixType.aMN, "matrix-aMN-0-0.bin", 2)), ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(0, 1)), FileTransferReady.message(Position.fromCoordinates(0, 1), BlockMatrixType.aMN, "data-0-1.dat", 3)), ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(0, 2)), FileTransferReady.message(Position.fromCoordinates(0, 2), BlockMatrixType.aMN, "data-0-2.dat", 3)), ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(1, 0)), FileTransferReady.message(Position.fromCoordinates(1, 0), BlockMatrixType.aMN, "data-1-0.dat", 3)), ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(1, 1)), FileTransferReady.message(Position.fromCoordinates(1, 1), BlockMatrixType.aMN, "data-1-1.dat", 2)), ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(1, 2)), FileTransferReady.message(Position.fromCoordinates(1, 2), BlockMatrixType.aMN, "data-1-2.dat", 1)), ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(2, 0)), FileTransferReady.message(Position.fromCoordinates(2, 0), BlockMatrixType.aMN, "data-2-0.dat", 1)), ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(2, 1)), FileTransferReady.message(Position.fromCoordinates(2, 1), BlockMatrixType.aMN, "data-2-1.dat", 1)), ActorRef.noSender());
                    mediator.tell(new DistributedPubSubMediator.Publish(FileTransferReady.getTopic(Position.fromCoordinates(2, 2)), FileTransferReady.message(Position.fromCoordinates(2, 2), BlockMatrixType.aMN, "data-2-2.dat", 2)), ActorRef.noSender());

                    return complete(StatusCodes.ACCEPTED, "message sent");
                })
            );
        //);
    }
}

class ProcessorHook extends Thread {

    ActorSystem system;

    public ProcessorHook(ActorSystem system) {
        this.system = system;
    }

    @Override
    public void run(){
        System.out.println("Shutting down actor system");
        system.terminate();
        System.out.println("Actor system terminated");

    }
}

