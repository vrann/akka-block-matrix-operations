package archive.handler;

import archive.message.Message;

interface BlockMessageHandler<M extends Message> {
    void handle(M message, int sectionId);
}
