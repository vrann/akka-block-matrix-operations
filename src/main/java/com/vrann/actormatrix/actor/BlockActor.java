package com.vrann.actormatrix.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.vrann.actormatrix.block.Block;

import java.util.List;

public class BlockActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private Block block;

    public BlockActor(Block block) {
        this.block = block;
        log.info("{} block for position {} started", block.getName(), block.getPosition());
        for (String subscription: block.getSubscriptions()) {
            mediator.tell(new DistributedPubSubMediator.Subscribe(subscription, getSelf()), getSelf());
            log.info("Block ({}) subscribed to {}", block.getPosition(), subscription);
        }
    }

    static public Props props(Block block) {
        return block.getProps();
    }

    public List<String> getTopicSubscriptions()
    {
        return block.getSubscriptions();
    }

    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    @Override
    public AbstractActor.Receive createReceive() {
        return block.getReceive(log, () -> {return getSelf();}, getContext().system());
    }
}
