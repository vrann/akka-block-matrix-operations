package com.vrann.actormatrix;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import akka.NotUsed;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.management.javadsl.AkkaManagement;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.complete;
import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.vrann.actormatrix.filetransfer.message.FileTransferReady;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

public class TopologyManager {

    private final SectionCoordinator sectionCoordinator;

    public TopologyManager(
            SectionCoordinator sectionCoordinator
    ) {
        this.sectionCoordinator = sectionCoordinator;
    }

    Thread processHook = new Thread() {
        @Override
        public void run() {
            sectionCoordinator.stop();
        }
    };

    public void run() throws IOException {

        sectionCoordinator.createSectionActors();
        sectionCoordinator.start();
        Runtime.getRuntime().addShutdownHook(processHook);

        //final Http http = sectionCoordinator.getHttpEndpoint();


        /*final Http http = Http.get(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = createRoute(system, materializer, sectionConfig.getSectionId())
                .flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("0.0.0.0", 2000), materializer);*/

    }

    /*private Route createRoute(ActorSystem system, ActorMaterializer materializer, int sectionId) {
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
    }*/
}

