package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.Message;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.message.SubscriptionMessage;
import com.vrann.actormatrix.cholesky.BlockMatrixType;
import com.vrann.actormatrix.cholesky.message.A11MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.L11MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.L21MatrixDataAvailable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class StateManagementTest {

    /*@Test
    void triggerEventHandlerState() {

        StateEventHandler.Builder<SubscriptionMessage> eventHandlerBuilder = StateEventHandler.newBuilder();
        StateEventHandler<SubscriptionMessage> eventHandler = eventHandlerBuilder.setName("Subscribers")
            .addExpect(StateEvent.RECEIVED, new SubscriptionMessage("topic1"))
            .addExpect(StateEvent.RECEIVED, new SubscriptionMessage("topic2"))
            .addExpect(StateEvent.RECEIVED, new SubscriptionMessage("topic3"))
            .addExpect(StateEvent.RECEIVED, new SubscriptionMessage("topic4"))
            .setCondition(StateEvent.RECEIVED, StateCondition.ALL, HandlerState.COMPLETE)
            .build();

        class TestInvoked {
            private boolean invoked = false;

            public void invoke() {
                this.invoked = true;
            }

            public boolean isInvoked() {
                return invoked;
            }
        }
        TestInvoked testInvoked = new TestInvoked();

        eventHandler.onComplete((i) -> {
            System.out.println("Completed state handler");
            testInvoked.invoke();
        });

        assertEquals(HandlerState.INIT, eventHandler.getState());

        eventHandler.triggerEvent(StateEvent.RECEIVED, new SubscriptionMessage("topic1"));
        eventHandler.triggerEvent(StateEvent.RECEIVED, new SubscriptionMessage("topic2"));
        eventHandler.triggerEvent(StateEvent.RECEIVED, new SubscriptionMessage("topic3"));
        eventHandler.triggerEvent(StateEvent.RECEIVED, new SubscriptionMessage("topic4"));

        assertEquals(HandlerState.COMPLETE, eventHandler.getState());
        assertTrue(testInvoked.isInvoked());
    }*/

    @Test
    void triggerEventHandlerState() {

        StateEventHandler.Builder<L21MatrixDataAvailable> l21EventHandlerBuilder = StateEventHandler.newBuilder();
        StateEventHandler<L21MatrixDataAvailable> l21EventHandler = l21EventHandlerBuilder.setName("L21")
                .addExpecMessage(StateEvent.RECEIVED, L21MatrixDataAvailable.class, Position.fromCoordinates(0, 0))
                .addExpecMessage(StateEvent.RECEIVED, L21MatrixDataAvailable.class, Position.fromCoordinates(1, 1))
                .addExpecMessage(StateEvent.RECEIVED, L21MatrixDataAvailable.class, Position.fromCoordinates(2, 2))

                .addExpecMessage(StateEvent.PROCESSED, L21MatrixDataAvailable.class, Position.fromCoordinates(0, 0))
                .addExpecMessage(StateEvent.PROCESSED, L21MatrixDataAvailable.class, Position.fromCoordinates(1, 1))
                .addExpecMessage(StateEvent.PROCESSED, L21MatrixDataAvailable.class, Position.fromCoordinates(2, 2))

                .setCondition(StateEvent.RECEIVED, StateCondition.ALL, HandlerState.RECEIVED)
                .setCondition(StateEvent.PROCESSED, StateCondition.ALL, HandlerState.COMPLETE)
                .build();

        StateEventHandler.Builder<L11MatrixDataAvailable> l11EventHandlerBuilder = StateEventHandler.newBuilder();
        StateEventHandler<L11MatrixDataAvailable> l11EventHandler = l11EventHandlerBuilder.setName("L11")
                .addExpecMessage(StateEvent.RECEIVED, L11MatrixDataAvailable.class, Position.fromCoordinates(0, 0))
                .addExpecMessage(StateEvent.RECEIVED, L11MatrixDataAvailable.class, Position.fromCoordinates(1, 1))
                .addExpecMessage(StateEvent.RECEIVED, L11MatrixDataAvailable.class, Position.fromCoordinates(2, 2))

                .addExpecMessage(StateEvent.PROCESSED, L11MatrixDataAvailable.class, Position.fromCoordinates(0, 0))
                .addExpecMessage(StateEvent.PROCESSED, L11MatrixDataAvailable.class, Position.fromCoordinates(1, 1))
                .addExpecMessage(StateEvent.PROCESSED, L11MatrixDataAvailable.class, Position.fromCoordinates(2, 2))

                .setCondition(StateEvent.RECEIVED, StateCondition.ALL, HandlerState.RECEIVED)
                .setCondition(StateEvent.PROCESSED, StateCondition.ALL, HandlerState.COMPLETE)
                .build();

        StateManagement stateMachine = StateManagement.newBuilder()
                .addSateHandler(l21EventHandler)
                .addSateHandler(l11EventHandler)
                .build();

        stateMachine.changeStateOnStateHandler("L21", HandlerState.RECEIVED, BlockState.L21_ALL_RECEIVED);
        stateMachine.changeStateOnStateHandler("L21", HandlerState.COMPLETE, BlockState.L21_CALCULATED);
        stateMachine.changeStateOnStateHandler("L11", HandlerState.RECEIVED, BlockState.L11_RECEIVED);
        stateMachine.changeStateOnStateHandler("L11", HandlerState.COMPLETE);
        stateMachine.onStateChange(EnumSet.of(BlockState.L21_CALCULATED, BlockState.L11_RECEIVED), () -> {});

                //.onStateChange(, HandlerState.RECEIVED, BlockState.L21_ALL_RECEIVED);


        class TestInvoked {
            private boolean invoked = false;

            public void invoke() {
                this.invoked = true;
            }

            public boolean isInvoked() {
                return invoked;
            }
        }
        TestInvoked testInvoked = new TestInvoked();

        eventHandler.onComplete((i) -> {
            System.out.println("Completed state handler");
            testInvoked.invoke();
        });

        assertEquals(HandlerState.INIT, eventHandler.getState());

        eventHandler.triggerEvent(StateEvent.RECEIVED, new SubscriptionMessage("topic1"));
        eventHandler.triggerEvent(StateEvent.RECEIVED, new SubscriptionMessage("topic2"));
        eventHandler.triggerEvent(StateEvent.RECEIVED, new SubscriptionMessage("topic3"));
        eventHandler.triggerEvent(StateEvent.RECEIVED, new SubscriptionMessage("topic4"));

        assertEquals(HandlerState.COMPLETE, eventHandler.getState());
        assertTrue(testInvoked.isInvoked());
    }

    void triggerEvent() {
//        StateManagement stateManagement = new StateManagement();
//        stateManagement.triggerEvent(StateEvent.RECEIVED, A11MatrixDataAvailable.create(
//                Position.fromCoordinates(0, 0), BlockMatrixType.A11, new File("test"), 0));
//        stateManagement.onStateChange(BlockState.A22_CALCULATED, () -> {
//
//        });

// state: INIT, COMPETE
//status: true|false
        // L21Applied.status(): true -- all L21 received and applied; false -- not all
        // L11.status == init}received|applied|sent

//INIT -> WAITING_FOR_L21 -> WAITING_FOR_L11 -> COMPLETE

        /*StateEventHandler<L11MatrixDataAvailable> l11MatrixDataAvailableStateEventHandler = StateEventHandler.newBuilder()
                .setName("L11")
                .addExpect(StateEvent.RECEIVED, new L11MatrixDataAvailable(Position.fromCoordinates(a, b)))
                .setCondition()
                .build();
        StateEventHandler<L21MatrixDataAvailable> l21MatrixDataAvailableStateEventHandler = StateEventHandler.newBuilder()
                .setName("L21")
                .addExpect(StateEvent.RECEIVED, new L11MatrixDataAvailable(Position.fromCoordinates(a, b)))
                .setCondition()
                .build();
        StateManagement stateManagement = new StateManagement();
        stateManagement
                .addHandler(l11MatrixDataAvailableStateEventHandler)
                .addHandler(l11MatrixDataAvailableStateEventHandler)
                .addRule("L21", "COMPLETE", "WAITING_FOR_L11")
                .addRule("L11", "COMPLETE", "COMPLETE")
                build();*/
    }
}