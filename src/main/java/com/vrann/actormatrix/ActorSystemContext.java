package com.vrann.actormatrix;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.pubsub.DistributedPubSub;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import com.typesafe.config.Config;

public class ActorSystemContext {

    private ActorSystem actorSystem;
    private LoggingAdapter log;
    private ActorMaterializer materializer;
    private ActorRef mediator;

    private ActorSystemContext(
            ActorSystem actorSystem
    ) {
        this.actorSystem = actorSystem;
        log = Logging.getLogger(actorSystem.eventStream(), "my.string");
//        log =  Logging.getLogger(actorSystem, actorSystem);
        materializer = ActorMaterializer.create(actorSystem);
        mediator = DistributedPubSub.get(actorSystem).mediator();
    }

    public static ActorSystemContext createFromActorSystem(ActorSystem actorSystem) {
        return new ActorSystemContext(actorSystem);
    }

    public static ActorSystemContext create(Config appConfig) {
        ActorSystem actorSystem = ActorSystem.create(
                "actor-system-section",
                appConfig
        );
        return new ActorSystemContext(actorSystem);
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public ActorRef getMediator() {
        return mediator;
    }

    public ActorMaterializer getMaterializer() {
        return materializer;
    }

    public LoggingAdapter getLog() {
        return log;
    }
}
