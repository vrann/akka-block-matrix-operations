package com.vrann.actormatrix.cholesky.handler;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.state.BlockMatrixState;
import com.vrann.actormatrix.block.state.StateManagement;
import com.vrann.actormatrix.cholesky.CholeskyEvent;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;
import com.vrann.actormatrix.cholesky.message.L21MatrixDataAvailable;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.A11;
import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.L21;

public class L21MatrixDataAvailableHandler implements BlockMatrixDataAvailableHandler<L21MatrixDataAvailable> {

    private LoggingAdapter log;
    private ActorRef mediator;
    private ActorMaterializer materializer;
    private final BlockMatrixState<CholeskyMatrixType, CholeskyEvent> stateMachine;

    public L21MatrixDataAvailableHandler(
            LoggingAdapter log,
            ActorRef mediator,
            ActorMaterializer materializer,
            BlockMatrixState<CholeskyMatrixType, CholeskyEvent> stateMachine
    ) {
        this.log = log;
        this.mediator = mediator;
        this.materializer = materializer;
        this.stateMachine = stateMachine;
    }

    public void handle(L21MatrixDataAvailable message, Position position, int sectionId, ActorRef selfReference) {
        log.info("Received L21MatrixDataAvailable message {}", message);
        stateMachine.triggerEvent(CholeskyEvent.RECEIVED, L21, position);
        /*if (message.getPosition().getX() != position.getX()
                || message.getPosition().getY() != position.getY()
        ) {
            log.info("Block coordinates {}, {} are not supported by the handler",
                    message.getPosition().getX(), message.getPosition().getY());
        }

        if ((message.getPosition().getX() == 0 && message.getPosition().getY() == 0)
                || message.getMatrixType() == BlockMatrixType.A22
        ) {
            log.info("This is either first A(1, 1) diagonal block or any other " +
                            "A(N, N) diagonal block which was already calculated {} {}",
                    message.getPosition().getX(), message.getPosition().getY());

            log.info("Received A11Ready message");
            try {
                DenseMatrix A11 = UnformattedMatrixReader
                        .<DenseMatrix>ofPositionAndMatrixType(
                                message.getPosition(), message.getMatrixType()
                        ).readMatrix(new DenseMatrixFactory());

                DenseMatrix L11 = Factorization.apply(A11);

                UnformattedMatrixWriter<DenseMatrix> writer = UnformattedMatrixWriter
                        .ofFileLocator(
                                MatrixTypePositionFileLocator::getFile,
                                message.getPosition(),
                                BlockMatrixType.L11
                        );
                writer.writeMatrix(L11);

                mediator.tell(new DistributedPubSubMediator.Publish(
                                FileTransferReady.getTopic(message.getPosition()),
                                FileTransferReady.message(
                                        message.getPosition(),
                                        BlockMatrixType.L11,
                                        MatrixTypePositionFileLocator
                                                .getFile(
                                                        message.getPosition(),
                                                        BlockMatrixType.L11
                                                ).toString(),
                                        sectionId)
                        ), selfReference
                );

            } catch (IOException exception) {
                log.error("File for the matrix block is not found {} in section {}",
                        exception.getMessage(), sectionId);
                throw new IOException("File for the matrix block is not found", exception);
            }
        }*/
    }
}
