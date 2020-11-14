package com.vrann.actormatrix.filetransfer.handler;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.LoggingAdapter;
import com.vrann.actormatrix.FileLocator;
import com.vrann.actormatrix.cholesky.message.BlockMatrixDataLoaded;
import com.vrann.actormatrix.filetransfer.message.FileTransferReady;
import com.vrann.actormatrix.filetransfer.message.FileTransferRequest;
import java.io.File;
import java.io.IOException;

public class FileTransferReadyHandler implements SectionMessageHandler<FileTransferReady> {

    private FileLocator fileLocator;
    private ActorRef mediator;
    private LoggingAdapter log;

    public FileTransferReadyHandler(
            LoggingAdapter log,
            ActorRef mediator,
            FileLocator fileLocator
    ) {
        this.fileLocator = fileLocator;
        this.mediator = mediator;
        this.log = log;
    }

    public void handle(
            FileTransferReady message,
            int currentSectionId,
            ActorRef selfReference
    ) throws IOException {
        //if message section id is different from the current section id

        log.info("Received FileTransferReady message {}, {}, {}, {}",
                FileTransferReady.getTopic(message.getPosition()),
                message.getSourceSectionId(),
                message.getMatrixType(),
                message.getPosition()
        );

        if (message.getSourceSectionId() != currentSectionId) {
            String topic = String.format("request-file-transfer-%d", message.getSourceSectionId());
            log.info("Requesting file transfer {}", topic);
            FileTransferRequest fileTransferRequest = new FileTransferRequest(
                    message.getPosition(),
                    message.getMatrixType(),
                    message.getFileName(),
                    message.getSourceSectionId());
            log.info("Publishing topic from FileTransferReadyHandler: {}, {}", topic, fileTransferRequest);
            mediator.tell(new DistributedPubSubMediator.Publish(topic, fileTransferRequest),
                    selfReference);
        } else {
            final File file = fileLocator.getMatrixBlockFilePath(message.getFileName());
            if (!file.exists()) {
                log.error("File for the matrix block is not found {}", file.getAbsolutePath());
                throw new IOException("File for the matrix block is not found");
            }
            BlockMatrixDataLoaded resultMessage = new BlockMatrixDataLoaded.Builder()
                    .setBlockMatrixType(message.getMatrixType())
                    .setFilePath(file)
                    .setPosition(message.getPosition())
                    .setSectionId(currentSectionId)
                    .build();
            log.info("File exists. Notification about available file is sent {}", resultMessage.getTopic());
            mediator.tell(new DistributedPubSubMediator.Publish(resultMessage.getTopic(), resultMessage),
                    selfReference);
        }
    }
}
