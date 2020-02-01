package archive.handler;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.Position;
import archive.message.L21MatrixDataAvailable;

public class L21ReadyHandler {

    public static void handle(
            L21MatrixDataAvailable message,
            Position position,
            ActorRef selfReference,
            LoggingAdapter log,
            ActorRef mediator,
            ActorMaterializer materializer
    ) {
        log.info("Received L21Ready message");

        // assert matrix A11 is present on the node -- means message FileTransferReady was already received
        // assert that the L21 matrix we received is for correct coordinates

        //DenseMatrix A11; //= read from file
        //DenseMatrix A22 = RowBlockUpdate.apply(message.getL21(), A11);
        //mediator.tell(new DistributedPubSubMediator.Publish(String.format("A11.ready-%d-%d", position.getX(), position.getY()), new A11Ready(A22)), selfReference);
        log.info("Sent A22Ready message");
    }
}
