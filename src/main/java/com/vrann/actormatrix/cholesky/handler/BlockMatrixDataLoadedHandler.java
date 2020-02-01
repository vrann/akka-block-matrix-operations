package com.vrann.actormatrix.cholesky.handler;

import akka.actor.ActorRef;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.message.BlockMatrixDataLoaded;

public interface BlockMatrixDataLoadedHandler<T extends BlockMatrixDataLoaded> {
    void handle(T message, Position position, int sectionId, ActorRef selfReference);
}
