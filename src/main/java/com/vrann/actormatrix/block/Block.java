package com.vrann.actormatrix.block;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import com.vrann.actormatrix.ActorSelfReference;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.actor.BlockActor;
import com.vrann.actormatrix.cholesky.message.A11MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.L11MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.aMNMatrixDataAvailable;

import java.util.ArrayList;
import java.util.List;

import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.*;

public interface Block {

    String getName();

    Props getProps();

    /**
     * Diagonal element starts with receiving aMN data available for it's position.
     * This is the message with the initial data for the block at the position.
     *
     * Then, unless the position of the block is (1, 1) diagonal element receives (K-1) messages of type L21,
     * where (K, K) is the position of the element
     */
    default List<String> getDiagonalSubscriptions() {
        var result = new ArrayList<String>();
        result.add(aMNMatrixDataAvailable.generateTopic(getPosition(), aMN));
        for (int i = 0; i < getPosition().getY(); i++) {
            result.add(A11MatrixDataAvailable.generateTopic(Position.fromCoordinates(getPosition().getX(), i), L21));
        }
        return result;
    }

    /**
     * Sub-Diagonal element starts with receiving aMN data available for it's position.
     * This is the message with the initial data for the block at the position.
     *
     * Then, unless the position of the block is not (M, 1) sub-diagonal element receives (K-1) messages of type L21,
     * where (M, K) is the position of the element
     *
     * Then, sub-diagonal element receives L11 message
     */
    default List<String> getSubDiagonalSubscriptions() {
        var result = getDiagonalSubscriptions();
        result.add(L11MatrixDataAvailable.generateTopic(
                Position.fromCoordinates(
                    getPosition().getY(),
                    getPosition().getY()
                ), L11));
        return result;
    }

    default ActorRef startActor(ActorSystem system) {
        return system.actorOf(BlockActor.props(this), getName());
    }

    default List<String> getSubscriptions() {
        return isDiagonal() ? getDiagonalSubscriptions() : getSubDiagonalSubscriptions();
    }

    default boolean isDiagonal() {
        return getPosition().getX() == getPosition().getY();
    }

    AbstractActor.Receive getReceive(LoggingAdapter log, ActorSelfReference selfReference, ActorSystem system);

    Position getPosition();

    BlockState status();

    //List<? extends BlockElementState> getState();

//    void when(BlockElementState state, Consumer<Message> fun);
//
//    void state(BlockElementState state);
//
//    void changeState(BlockElementState state);
}
