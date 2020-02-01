package com.vrann.actormatrix.block;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import com.vrann.actormatrix.ActorSelfReference;
import com.vrann.actormatrix.Position;

import java.util.List;

public interface Block {

    String getName();

    Props getProps();

    List<String> getSubscriptions();

    AbstractActor.Receive getReceive(LoggingAdapter log, ActorSelfReference selfReference, ActorSystem system);

    Position getPosition();

    //List<? extends BlockElementState> getState();

//    void when(BlockElementState state, Consumer<Message> fun);
//
//    void state(BlockElementState state);
//
//    void changeState(BlockElementState state);
}
