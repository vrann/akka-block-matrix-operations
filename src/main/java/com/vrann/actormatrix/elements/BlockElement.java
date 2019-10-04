package com.vrann.actormatrix.elements;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import com.vrann.actormatrix.*;
import com.vrann.actormatrix.message.Message;

import java.util.List;
import java.util.function.Consumer;

public interface BlockElement {

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
