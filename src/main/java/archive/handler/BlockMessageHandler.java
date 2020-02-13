package archive.handler;

import com.vrann.actormatrix.Message;

interface BlockMessageHandler<M extends Message> {
    void handle(M message, int sectionId);
}
