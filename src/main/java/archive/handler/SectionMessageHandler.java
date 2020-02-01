package archive.handler;

import akka.actor.ActorRef;
import archive.message.Message;

import java.io.IOException;

public interface SectionMessageHandler<M extends Message> {
    void handle(M message, int sectionId, ActorRef selfReference) throws IOException;
}
