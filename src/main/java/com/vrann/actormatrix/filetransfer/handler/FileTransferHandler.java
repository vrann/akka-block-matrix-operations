package com.vrann.actormatrix.filetransfer.handler;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.vrann.actormatrix.FileLocator;
import com.vrann.actormatrix.cholesky.message.BlockMatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.aMNMatrixDataAvailable;
import com.vrann.actormatrix.filetransfer.message.FileTransfer;

import java.io.File;
import java.util.concurrent.CompletionStage;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.aMN;

/**
 * Handles files sent by the remote actor
 */
public class FileTransferHandler implements SectionMessageHandler<FileTransfer> {

    private FileLocator fileLocator;
    private ActorRef mediator;
    private ActorMaterializer materializer;
    private LoggingAdapter log;

    public FileTransferHandler(
           LoggingAdapter log,
           ActorRef mediator,
           ActorMaterializer materializer,
           FileLocator fileLocator
    ) {
        this.fileLocator = fileLocator;
        this.mediator = mediator;
        this.materializer = materializer;
        this.log = log;
    }

    public static class Builder {

        private FileLocator fileLocator;
        private ActorRef mediator;
        private ActorMaterializer materializer;
        private LoggingAdapter log;


        public Builder setFileLocator(FileLocator fileLocator) {
            this.fileLocator = fileLocator;
            return this;
        }

        public Builder setMediator(ActorRef mediator) {
            this.mediator = mediator;
            return this;
        }

        public Builder setMaterializer(ActorMaterializer materializer) {
            this.materializer = materializer;
            return this;
        }

        public Builder setLoger(LoggingAdapter log) {
            this.log = log;
            return this;
        }

        public FileTransferHandler build() {
            return new FileTransferHandler(log, mediator, materializer, fileLocator);
        }
    }

    /**
     * Handles the actual transfer of the file from the remote actor.
     *
     * Remote actor initiates transfer by sending the file back to the sender actor. When sender actor receives the file
     * it invokes this handler. By default file is always written to disk
     *
     * @param message
     * @param currentSectionId
     * @param selfReference
     */
    public void handle(
        FileTransfer message,
        int currentSectionId,
        ActorRef selfReference
    ) {
        //test initiate fake transfer
        //if (coordinator.positions().contains(message.position)) {
        //if message sent to itself
        final File file = fileLocator.getMatrixBlockFilePath(message.getFileName());
        Sink<ByteString, CompletionStage<IOResult>> fileSink = FileIO.toFile(file);
        message.getSourceRef().getSource().runWith(fileSink, materializer);
        log.info("File is written to path {}", file);
        log.info("Matrix type: {}", message.getMatrixType());

        BlockMatrixDataAvailable resultMessage = new BlockMatrixDataAvailable.Builder()
                .setBlockMatrixType(message.getMatrixType())
                .setFilePath(file)
                .setPosition(message.getPosition())
                .setSectionId(currentSectionId)
                .build();

        log.info("Matrix data is written to file. Notification about available file is sent {}", resultMessage.getTopic());
        mediator.tell(new DistributedPubSubMediator.Publish(resultMessage.getTopic(), resultMessage), selfReference);
    }

}
