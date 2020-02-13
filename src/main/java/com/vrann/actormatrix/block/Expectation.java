package com.vrann.actormatrix.block;

import com.vrann.actormatrix.cholesky.message.L21MatrixDataAvailable;

public interface Expectation<T> {
    void receive(T message);

    boolean areAllReceived();
}
