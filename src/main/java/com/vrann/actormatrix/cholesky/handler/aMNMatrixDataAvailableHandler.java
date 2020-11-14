package com.vrann.actormatrix.cholesky.handler;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.state.BlockMatrixState;
import com.vrann.actormatrix.cholesky.CholeskyEvent;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;
import com.vrann.actormatrix.cholesky.message.aMNMatrixDataAvailable;
import java.util.Objects;
import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.aMN;

public class aMNMatrixDataAvailableHandler implements BlockMatrixDataAvailableHandler<aMNMatrixDataAvailable> {

    private LoggingAdapter log;
    private ActorRef mediator;
    private ActorMaterializer materializer;
    private final BlockMatrixState<CholeskyMatrixType> stateMachine;

    public aMNMatrixDataAvailableHandler(
            LoggingAdapter log,
            ActorRef mediator,
            ActorMaterializer materializer,
            BlockMatrixState<CholeskyMatrixType> stateMachine
    ) {
        this.log = log;
        this.mediator = mediator;
        this.materializer = materializer;
        this.stateMachine = stateMachine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        aMNMatrixDataAvailableHandler that = (aMNMatrixDataAvailableHandler) o;
        return Objects.equals(log, that.log) &&
                Objects.equals(mediator, that.mediator) &&
                Objects.equals(materializer, that.materializer) &&
                Objects.equals(stateMachine, that.stateMachine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(log, mediator, materializer, stateMachine);
    }

    public void handle(aMNMatrixDataAvailable message, Position position, int sectionId, ActorRef selfReference) {
        log.info("Received aMNMatrixDataAvailable message {}", message);
        stateMachine.triggerEvent(CholeskyEvent.RECEIVED, aMN, position);
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
