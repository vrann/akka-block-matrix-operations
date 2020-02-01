package com.vrann.actormatrix.cholesky.handler;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.message.A11MatrixDataAvailable;
import com.vrann.actormatrix.filetransfer.message.FileTransferReady;

public class A11MatrixDataAvailableHandler implements BlockMatrixDataAvailableHandler<A11MatrixDataAvailable> {

    private LoggingAdapter log;
    private ActorRef mediator;
    private ActorMaterializer materializer;

    public A11MatrixDataAvailableHandler(
        LoggingAdapter log,
        ActorRef mediator,
        ActorMaterializer materializer
    ) {
        this.log = log;
        this.mediator = mediator;
        this.materializer = materializer;
    }

    public void handle(A11MatrixDataAvailable message, Position position, int sectionId, ActorRef selfReference) {
        log.info("Received A11MatrixDataAvailable message");
        log.info(FileTransferReady.getTopic(message.getPosition()));
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
