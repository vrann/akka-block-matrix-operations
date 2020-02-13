package com.vrann.actormatrix.filetransfer.handler;

import akka.actor.ActorRef;
import com.vrann.actormatrix.filetransfer.message.FileTransferRequest;

public interface MessageHandler<M extends FileTransferRequest> {

    void handle(
            M message,
            ActorRef sender,
            ActorRef selfReference
    );
}
