package com.vrann.actormatrix.handler;

import akka.actor.ActorRef;
import com.vrann.actormatrix.message.Message;

public interface MessageHandler<M extends Message> {

    void handle(
            M message,
            ActorRef sender,
            ActorRef selfReference
    );
}
