package archive.elements;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.*;
import com.vrann.actormatrix.actor.BlockActor;
import com.vrann.actormatrix.block.Block;
import archive.message.A11Ready;
import com.vrann.actormatrix.filetransfer.message.FileTransferReady;
import com.vrann.actormatrix.filetransfer.message.FileTransferRequest;

import java.util.Arrays;
import java.util.List;

public class L11Element implements Block {

    private Position position;
    private ActorRef mediator;

    public L11Element(Position position, ActorRef mediator) {
        this.position = position;
        this.mediator = mediator;
    }

    public String getName()
    {
        return String.format("L11-transformation-%d-%d-actor", position.getX(), position.getY());
    }

    @Override
    public Props getProps() {
        return Props.create(BlockActor.class, () -> new BlockActor(this));
    }

    public List<String> getSubscriptions()
    {
        return Arrays.asList(new String[]{
                "A11.ready",
                "logs",
                String.format("A11-data-ready-%d-%d", position.getX(), position.getY())
        });
    }

    @Override
    public Position getPosition() {
        return position;
    }

    public AbstractActor.Receive getReceive(LoggingAdapter log, ActorSelfReference selfReference, ActorSystem system)
    {
        ReceiveBuilder builder = new ReceiveBuilder();
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        return builder.match(A11Ready.class, message -> {
            log.info("Received A11Ready message");
            //DenseMatrix L11 = Factorization.apply(message.getA11());
            //mediator.tell(new DistributedPubSubMediator.Publish(String.format("L11.ready-%d-%d", position.getX(), position.getY()), new L11Ready(L11)), selfReference.getSelfInstance());
            log.info("Sent L11Ready message");
        }).match(FileTransferReady.class, message -> {
            //if message section id is different from the current section id
            log.info("Received FileTransferReady message");
            log.info("Publishing request-file-transfer-{}", message.getSourceSectionId());
            mediator.tell(new DistributedPubSubMediator.Publish(String.format("request-file-transfer-%d", message.getSourceSectionId()), new FileTransferRequest(message.getPosition(), message.getMatrixType(), message.getFileName(), message.getSourceSectionId())), selfReference.getSelfInstance());
        })
//                .match(FileTransfer.class, message -> {
//                    FileTransferHandler.handle(message, position, selfReference.getSelfInstance(), log, mediator, materializer);
//                })
                .match(DistributedPubSubMediator.SubscribeAck.class, msg -> log.info("subscribed    "))
                .matchAny(o -> log.info("received unknown message {}", o.getClass()))
                .build();
    }

}
