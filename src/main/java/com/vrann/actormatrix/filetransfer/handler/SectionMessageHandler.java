package com.vrann.actormatrix.filetransfer.handler;

import akka.actor.ActorRef;
import com.vrann.actormatrix.Message;

import java.io.IOException;

public interface SectionMessageHandler<M extends Message> {
    void handle(M message, int sectionId, ActorRef selfReference) throws IOException;
}
