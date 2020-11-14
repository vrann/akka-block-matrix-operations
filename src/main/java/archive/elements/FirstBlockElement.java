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
import com.vrann.actormatrix.block.BlockState;
import com.vrann.actormatrix.filetransfer.message.FileTransfer;
import com.vrann.actormatrix.filetransfer.message.FileTransferReady;
import com.vrann.actormatrix.filetransfer.message.FileTransferRequest;

import java.util.Arrays;
import java.util.List;

public class FirstBlockElement implements Block {

    private Position position;
    private ActorRef mediator;

    public FirstBlockElement(Position position, ActorRef mediator) {
        this.position = position;
        this.mediator = mediator;
    }

    public String getName()
    {
        return String.format("first-%d-%d-actor", position.getX(), position.getY());
    }

    @Override
    public Props getProps() {
        return Props.create(BlockActor.class, () -> new BlockActor(this));
    }

    public List<String> getSubscriptions()
    {
        return Arrays.asList(new String[]{
                "A11.first.ready",
                "logs",
                String.format("data-ready-%d-%d", position.getX(), position.getY())
        });
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public BlockState status() {
        return null;
    }

    public AbstractActor.Receive getReceive(LoggingAdapter log, ActorSelfReference selfReference, ActorSystem system)
    {
        ReceiveBuilder builder = new ReceiveBuilder();
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        return builder.match(String.class, s -> {
            if (s.equals("L11.ready")) {
                log.info("Read L11 computed {}", s);
                log.info("Calculate L21 {}", s);
                log.info("Send L21 {}", s);

                //what topic should it be
                //should topic have the cell address so that actor can filter out what cells to listen to
                //or there is any other mechanism in actors to selectively process messages


                //mediator.tell(new DistributedPubSubMediator.Publish("L11.ready", "L11.ready"), getSelf());
            }
            log.info("Received String message: {}", s);
        }).match(FileTransferReady.class, message -> {
            //if message section id is different from the current section id
            log.info("Received FileTransferReady message");
            log.info("Publishing request-file-transfer-{}", message.getSourceSectionId());
            mediator.tell(new DistributedPubSubMediator.Publish(String.format("request-file-transfer-%d", message.getSourceSectionId()), new FileTransferRequest(message.getPosition(), message.getMatrixType(), message.getFileName(), message.getSourceSectionId())), selfReference.getSelfInstance());
        })
        .match(FileTransfer.class, message -> {
//            StringBuilder pathBuilder = (new StringBuilder())
//                    .append(System.getProperty("user.home"))
//                    .append("/.actorchoreography/")
//                    //.append(message.fileName);
//            final Path file = Paths.get(pathBuilder.toString());
//            Sink<ByteString, CompletionStage<IOResult>> fileSink = FileIO.toPath(file);
//            //message.sourceRef.getSource().runWith(fileSink, materializer);
        })
        .match(DistributedPubSubMediator.SubscribeAck.class, msg -> log.info("subscribed    "))
        .matchAny(o -> log.info("received unknown message {}", o.getClass()))
        .build();
    }
}
