package com.vrann.actormatrix.cholesky.handler;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.ActorSystemContext;
import com.vrann.actormatrix.cholesky.BlockMatrixType;

public class HandlerFactory {

    private LoggingAdapter log;
    private ActorRef mediator;
    private ActorMaterializer materializer;

    private HandlerFactory(
        LoggingAdapter log,
        ActorRef mediator,
        ActorMaterializer materializer
    ) {
        this.log = log;
        this.mediator = mediator;
        this.materializer = materializer;
    }

    public static HandlerFactory create(
            ActorSystemContext context
    ) {
        LoggingAdapter log = context.getLog();
        ActorRef mediator = context.getMediator();
        ActorMaterializer materializer = context.getMaterializer();
        return new HandlerFactory(log, mediator, materializer);
    }

    public BlockMatrixDataAvailableHandler getHandler(BlockMatrixType matrixType) throws Exception {
        switch (matrixType) {
            case A11: return new A11MatrixDataAvailableHandler(log, mediator, materializer);
            case L11: return new L11MatrixDataAvailableHandler(log, mediator, materializer);
            case L21: return new L21MatrixDataAvailableHandler(log, mediator, materializer);
            case A22: return new A22MatrixDataAvailableHandler(log, mediator, materializer);
            default:
                throw new Exception(matrixType + " is not supported");
        }
    }
}
