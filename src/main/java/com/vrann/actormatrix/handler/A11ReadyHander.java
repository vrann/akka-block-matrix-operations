package com.vrann.actormatrix.handler;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.LoggingAdapter;
import com.vrann.actormatrix.SectionCoordinator;
import com.vrann.actormatrix.message.BlockMatrixDataAvailable;
import com.vrann.actormatrix.message.FileTransferReady;
import com.vrann.dataformat.*;
import org.apache.spark.ml.linalg.DenseMatrix;
import com.vrann.actormatrix.Position;
import com.vrann.blockedcholesky.operation.BlockMatrixType;
import com.vrann.blockedcholesky.operation.Factorization;
import java.io.IOException;

public class A11ReadyHander {

    private ActorRef selfReference;
    private LoggingAdapter log;
    private ActorRef mediator;
    private SectionCoordinator sectionCoordinator;
    UnformattedMatrixReader<DenseMatrix> reader;
    UnformattedMatrixWriter<DenseMatrix> writer;

    public A11ReadyHander(
          ActorRef selfReference,
          LoggingAdapter log,
          ActorRef mediator,
          SectionCoordinator sectionCoordinator,
          UnformattedMatrixReader<DenseMatrix> reader,
          UnformattedMatrixWriter<DenseMatrix> writer
    ) {
        this.selfReference = selfReference;
        this.log = log;
        this.mediator = mediator;
        this.sectionCoordinator = sectionCoordinator;
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * 0. check that the matrix block is (0, 0) and executed on correct actor
     * 1. Read matrix from file
     * 2. Factorize matrix
     * 3. Write matrix to the file
     * 4. Send message that the matrix data is available
     *
     * This event can occur either if element is 0,0 or if it is on diagonal;
     * However only in situation when it is not 0,0 element we need to check that all L21 applications were processed
     * However A11 ready is issued only when A11 was calculated, so if it is ready it is ready and we need to process it
     * We either need to check that coordinates of the current block is the same as the A11 or we just need to have topics with the coordinates
     * ideally we need both, so let's check it here
     *
     * @param message
     * @param actorPosition
     * @throws IOException
     */
    public void handle(
            BlockMatrixDataAvailable message,
            Position actorPosition
    ) throws IOException {

        //why should all matrix be passed as a message is it is on the disk already
        //ideally this message means that the matrix is on the disk
        //anyway let's write something then improve

        if (message.getPosition().getX() != actorPosition.getX()
                        || message.getPosition().getY() != actorPosition.getY()
        ) {
            log.info("Block coordinates {}, {} are not supported by the handler",
                    message.getPosition().getX(), message.getPosition().getY());
        }

        if ((message.getPosition().getX() == 0 && message.getPosition().getY() == 0)
                        || message.getMatrixType() == BlockMatrixType.A22
        ) {
            log.info("This is either first A(1, 1) diagonal block or any other " +
                            "A(N, N) diagonal block which was already calculated",
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
                                MatrixTypePositionFileLocator.getFile(message.getPosition(), BlockMatrixType.L11).toString(),
                                sectionCoordinator.getSectionId())
                        ), selfReference
                );

            } catch (IOException exception) {
                log.error("File for the matrix block is not found {}", exception.getMessage());
                throw new IOException("File for the matrix block is not found", exception);
            }
        }
    }
}
