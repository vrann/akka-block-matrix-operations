package com.vrann.actormatrix.handler;

import com.vrann.actormatrix.message.Message;

interface BlockMessageHandler<M extends Message> {
    void handle(M message, int sectionId);
}
