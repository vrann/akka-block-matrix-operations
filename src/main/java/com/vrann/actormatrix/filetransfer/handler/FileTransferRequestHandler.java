package com.vrann.actormatrix.filetransfer.handler;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.SourceRef;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamRefs;
import akka.util.ByteString;
import com.vrann.actormatrix.*;
import archive.handler.MessageHandler;
import com.vrann.actormatrix.filetransfer.message.FileTransfer;
import com.vrann.actormatrix.filetransfer.message.FileTransferRequest;
import scala.concurrent.ExecutionContextExecutor;

import java.io.File;
import java.util.concurrent.CompletionStage;

public class FileTransferRequestHandler implements MessageHandler<FileTransferRequest> {

    private FileLocator fileLocator;
    private ActorMaterializer materializer;
    private LoggingAdapter log;
    private ExecutionContextExecutor dispatcher;

    public FileTransferRequestHandler(
        LoggingAdapter log,
        ActorMaterializer materializer,
        ExecutionContextExecutor dispatcher,
        FileLocator fileLocator
    ) {
        this.fileLocator = fileLocator;
        this.materializer = materializer;
        this.log = log;
        this.dispatcher = dispatcher;
    }

    /**
     * This method handles the part which sends the file over
     * Receiving side should be implemented as a separate receiving actor
     *
     * @param message FileTransferRequest
     * @param sender ActorRef
     */
    public void handle(FileTransferRequest message, ActorRef sender, ActorRef selfReference)
    {
        log.info("Received request for file {}", message.getFileName());
        final File file = fileLocator.getMatrixBlockFilePath(message.getFileName());
        Source<ByteString, CompletionStage<IOResult>> fileSource = FileIO.fromFile(file);
        CompletionStage<SourceRef<ByteString>> fileRef = fileSource.runWith(StreamRefs.sourceRef(), materializer);

        System.out.println(sender.path());
        Patterns.pipe(
                fileRef.thenApply(
                        ref -> new FileTransfer(
                                    message.getFileName(),
                                    message.getMatrixType(),
                                    message.getPosition(),
                                    ref
                        )
                ), dispatcher)
                .to(sender, selfReference);
    }
}
