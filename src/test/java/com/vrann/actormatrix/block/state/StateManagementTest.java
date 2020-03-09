package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockStateDefault;
import com.vrann.actormatrix.cholesky.CholeskyBlockState;
import com.vrann.actormatrix.cholesky.CholeskyEvent;
import com.vrann.actormatrix.cholesky.CholeskyMatrixType;
import com.vrann.actormatrix.cholesky.message.L11MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.L21MatrixDataAvailable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.EnumSet;

//import static com.vrann.actormatrix.block.BlockMatrixType.L11;
//import static com.vrann.actormatrix.cholesky.CholeskyBlockState.*;
import static com.vrann.actormatrix.cholesky.CholeskyBlockState.*;
import static com.vrann.actormatrix.cholesky.CholeskyEvent.PROCESSED;
import static com.vrann.actormatrix.cholesky.CholeskyEvent.RECEIVED;
import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.A11;
import static com.vrann.actormatrix.cholesky.CholeskyMatrixType.L11;
import static org.junit.jupiter.api.Assertions.*;

class StateManagementTest {

    /*

                                             ____________
                                            |            |
L21 (1, i), (2, i), .., (i-1, i)    ----->  | A11 (i, i) |
                                            |____________|
                                                  |
                                                  |
                                                  V
                                               L11 (i, i)
 */
/*enum DiagonalBlockState {
    ACTOR_STARTED,
    BLOCK_SUBSCRIBED,
    ANN_DATA_READY,
    A22_DATA_READY,
    A11_DATA_READY,
    L11_CALCULATED
}


/*

Sub-diagonal Blocks

                                                L11 (i, i)
                                                  |
                                                  |
                                             _____V______
                                            |           |
L21 (1, j), (2, j), .., (i-1, j)    ----->  | aMN (i,j) | ----> L21 (i,j)
                                            |___________|

A22 is not an external state, it is aMN with applied L21
We cannot apply L11 before all L21 received and applied, We can only receive it

 */


/*enum SubDiagonalBlockState {
    ACTOR_STARTED,
    BLOCK_SUBSCRIBED,
    AMN_DATA_READY,
    A22_DATA_READY,
    L21_CALCULATED
}

enum DiagonalBlockTransition {

    SUBSCRIBE(DiagonalBlockState.ACTOR_STARTED, DiagonalBlockState.BLOCK_SUBSCRIBED),
    INIT_DATA(DiagonalBlockState.BLOCK_SUBSCRIBED, DiagonalBlockState.ANN_DATA_READY),
    RECEIVE_A22(DiagonalBlockState.ANN_DATA_READY, DiagonalBlockState.A22_DATA_READY);

    private final DiagonalBlockState from;
    private final DiagonalBlockState to;

    DiagonalBlockTransition(DiagonalBlockState from, DiagonalBlockState to) {
        this.from = from;
        this.to = to;
    }
}

enum ExpectationType {
    SUBSCRIPTIONS,
    APPLY_L21,
    RECEIVE_L21
}
*/

    @Test
    void testBlockState3x3_0_0() {
        BlockMatrixState<CholeskyMatrixType, CholeskyEvent> stateManagement =
                BlockMatrixState
                .<CholeskyMatrixType, CholeskyEvent>expected(A11, Position.fromCoordinates(0, 0))
                .when(A11, RECEIVED)
                    .one() //.one(Position) //set(List<Positions>)
                    .setState(A11, A11_RECEIVED) //run(Consumer<>)
                .when(A11, PROCESSED)
                    .one()
                    .setState(A11, COMPLETE)
                //.onCondition()
                .build();
        assertEquals(BlockStateDefault.INIT, stateManagement.getState());
        stateManagement.triggerEvent(RECEIVED, A11, Position.fromCoordinates(0, 0));
        assertEquals(A11_RECEIVED, stateManagement.getState());
        stateManagement.triggerEvent(PROCESSED, A11, Position.fromCoordinates(0, 0));
        assertEquals(COMPLETE, stateManagement.getState());
    }

    @Test
    void testBlockState3x3_0_1() {
        BlockMatrixState<CholeskyMatrixType, CholeskyEvent> stateManagement =
                BlockMatrixState.<CholeskyMatrixType, CholeskyEvent>expected(L11, Position.fromCoordinates(0, 0))
                .when(L11, RECEIVED)
                .all() //.one(Position) //set(List<Positions>)
                .setState(L11, L11_RECEIVED) //run(Consumer<>)
                .onCondition(
                        (new BlockMatrixState.Condition<>(L11, RECEIVED)).all(),
                        (condition, state) -> {
                            System.out.println("all received");
                        }
                )
                .when(L11, PROCESSED)
                .all() //.one(Position) //set(List<Positions>)
                .setState(L11, L11_CALCULATED) //run(Consumer<>)
                .build();

        assertEquals(BlockStateDefault.INIT, stateManagement.getState());
        stateManagement.triggerEvent(RECEIVED, L11, Position.fromCoordinates(0, 0));
        assertEquals(L11_RECEIVED, stateManagement.getState());
        stateManagement.triggerEvent(PROCESSED, L11, Position.fromCoordinates(0, 0));
        assertEquals(COMPLETE, stateManagement.getState());
    }

    @Test
    void testBlockStateException() {
        BlockMatrixState<CholeskyMatrixType, CholeskyEvent> stateManagement =
                BlockMatrixState.<CholeskyMatrixType, CholeskyEvent>expected(L11, Position.fromCoordinates(0, 0))
                        .build();

        assertEquals(BlockStateDefault.INIT, stateManagement.getState());
        stateManagement.triggerEvent(RECEIVED, L11, Position.fromCoordinates(0, 0));
        stateManagement.triggerEvent(RECEIVED, L11, Position.fromCoordinates(0, 0));

        stateManagement.triggerEvent(RECEIVED, A11, Position.fromCoordinates(0, 0));

    }

    @Test
    void testBlockState3x3_1_1() {

    }

    @Test
    void testBlockState3x3_0_2() {

    }

    @Test
    void testBlockState3x3_1_2() {

    }

    @Test
    void testBlockState3x3_2_2() {

    }

    @Test
    void clientExemplar() {

        //final BlockMatrixState<CholeskyMatrixType, CholeskyEvent> stateManagement = new BlockMatrixState<>();

        /*MatrixTypeHandler L11Handler = new MatrixTypeHandler(BlockMatrixType.L11);
        stateManagement.addHandler(BlockMatrixType.L11, L11Handler);*/

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

        BlockMatrixState.TransitionRulesBuilder<CholeskyMatrixType, CholeskyEvent> stateManagement = BlockMatrixState
                .<CholeskyMatrixType, CholeskyEvent>expected(L11, Position.fromCoordinates(1, 1))
                .expected(L11, Position.fromCoordinates(2, 2))
                .expected(L11, Position.fromCoordinates(3, 3))
                .expected(L11, Position.fromCoordinates(0, 0));

        stateManagement
                .when(L11, RECEIVED)
                .all() //.one(Position) //set(List<Positions>)
                .setState(L11, L11_RECEIVED); //run(Consumer<>)

        stateManagement.onCondition(
                (new BlockMatrixState.Condition<>(L11, RECEIVED)).one(),
                (condition, state) -> {
                    System.out.println("l11 received");
                }
        );

        BlockMatrixState<CholeskyMatrixType, CholeskyEvent> blockMatrixState = stateManagement
                .when(L11, PROCESSED)
                .all() //.one(Position) //set(List<Positions>)
                .setState(L11, L11_CALCULATED) //run(Consumer<>)
                .build();



        assertEquals(BlockStateDefault.INIT, blockMatrixState.getState());

        blockMatrixState.triggerEvent(RECEIVED, L11, Position.fromCoordinates(1, 1));
        blockMatrixState.triggerEvent(RECEIVED, L11, Position.fromCoordinates(2, 2));
        blockMatrixState.triggerEvent(PROCESSED, L11, Position.fromCoordinates(1, 1));
        blockMatrixState.triggerEvent(PROCESSED, L11, Position.fromCoordinates(2, 2));

        assertEquals(BlockStateDefault.INIT, blockMatrixState.getState());

        blockMatrixState.triggerEvent(RECEIVED, L11, Position.fromCoordinates(3, 3));
        blockMatrixState.triggerEvent(RECEIVED, L11, Position.fromCoordinates(0, 0));

        assertEquals(CholeskyBlockState.L11_RECEIVED, blockMatrixState.getState());


        assertEquals(CholeskyBlockState.L11_RECEIVED, blockMatrixState.getState());
        blockMatrixState.triggerEvent(PROCESSED, L11, Position.fromCoordinates(3, 3));
        blockMatrixState.triggerEvent(PROCESSED, L11, Position.fromCoordinates(0, 0));
        assertEquals(L11_CALCULATED, blockMatrixState.getState());
    }




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

//    @Test
//    void triggerEventHandlerState() {
//
//        StateEventHandler.Builder<L21MatrixDataAvailable> l21EventHandlerBuilder = StateEventHandler.newBuilder();
//        StateEventHandler<L21MatrixDataAvailable> l21EventHandler = l21EventHandlerBuilder.setName("L21")
//                .addExpectMessage(StateEvent.RECEIVED, L21MatrixDataAvailable.class, Position.fromCoordinates(0, 0))
//                .addExpectMessage(StateEvent.RECEIVED, L21MatrixDataAvailable.class, Position.fromCoordinates(1, 1))
//                .addExpectMessage(StateEvent.RECEIVED, L21MatrixDataAvailable.class, Position.fromCoordinates(2, 2))
//
//                .addExpectMessage(StateEvent.PROCESSED, L21MatrixDataAvailable.class, Position.fromCoordinates(0, 0))
//                .addExpectMessage(StateEvent.PROCESSED, L21MatrixDataAvailable.class, Position.fromCoordinates(1, 1))
//                .addExpectMessage(StateEvent.PROCESSED, L21MatrixDataAvailable.class, Position.fromCoordinates(2, 2))
//
//                /*.onEvent(StateEvent.RECEIVED).evaluateState((handler) -> {
//                    if (handler.areAllReceived()) {
//                        handler.setNewState(HandlerState.RECEIVED);
//                    }
//                })
//                .onEvent(StateEvent.PROCESSED).evaluateState(() -> {
//                    if (areAllProcessed()) {
//                        setNewState(HandlerState.COMPLETE);
//                    }
//                })*/
//                //.setCondition(StateEvent.RECEIVED, StateCondition.ALL, HandlerState.RECEIVED)
//                //.setCondition(StateEvent.PROCESSED, StateCondition.ALL, HandlerState.COMPLETE)
//                .build();
//
//        StateEventHandler.Builder<L11MatrixDataAvailable> l11EventHandlerBuilder = StateEventHandler.newBuilder();
//        StateEventHandler<L11MatrixDataAvailable> l11EventHandler = l11EventHandlerBuilder.setName("L11")
//                .addExpectMessage(StateEvent.RECEIVED, L11MatrixDataAvailable.class, Position.fromCoordinates(0, 0))
//                .addExpectMessage(StateEvent.RECEIVED, L11MatrixDataAvailable.class, Position.fromCoordinates(1, 1))
//                .addExpectMessage(StateEvent.RECEIVED, L11MatrixDataAvailable.class, Position.fromCoordinates(2, 2))
//
//                .addExpectMessage(StateEvent.PROCESSED, L11MatrixDataAvailable.class, Position.fromCoordinates(0, 0))
//                .addExpectMessage(StateEvent.PROCESSED, L11MatrixDataAvailable.class, Position.fromCoordinates(1, 1))
//                .addExpectMessage(StateEvent.PROCESSED, L11MatrixDataAvailable.class, Position.fromCoordinates(2, 2))
//
//                .setCondition(StateEvent.RECEIVED, StateCondition.ALL, HandlerState.RECEIVED)
//                .setCondition(StateEvent.PROCESSED, StateCondition.ALL, HandlerState.COMPLETE)
//                .build();
//
//        StateManagement stateMachine = StateManagement.newBuilder()
//                .addSateHandler(L21MatrixDataAvailable.class, l21EventHandler)
//                .addSateHandler(L11MatrixDataAvailable.class, l11EventHandler)
//                .build();
//
//        class TestInvoked {
//            private boolean invoked = false;
//
//            public void invoke() {
//                this.invoked = true;
//            }
//
//            public boolean isInvoked() {
//                return invoked;
//            }
//        }
//        TestInvoked testInvoked = new TestInvoked();
//
////        stateMachine.changeStateOnStateHandler("L21", HandlerState.RECEIVED, BlockState.L21_ALL_RECEIVED);
////        stateMachine.changeStateOnStateHandler("L21", HandlerState.COMPLETE, BlockState.L21_CALCULATED);
////        stateMachine.changeStateOnStateHandler("L11", HandlerState.RECEIVED, BlockState.L11_RECEIVED);
////        stateMachine.changeStateOnStateHandler("L11", HandlerState.COMPLETE);
//        stateMachine.onStateChange((currentState) -> {
//            EnumSet<CholeskyBlockState> filter = EnumSet.of(CholeskyBlockState.L21_CALCULATED, CholeskyBlockState.L11_RECEIVED);
//            long existingStatesCount = currentState.stream().filter((element) -> filter.contains(element)).count();
//            if (existingStatesCount != filter.size()) {
//                return;
//            }
//            testInvoked.invoke();
//        });
//
//                //.onStateChange(, HandlerState.RECEIVED, BlockState.L21_ALL_RECEIVED);
//
//
//
//
//
//        /*eventHandler.onComplete((i) -> {
//            System.out.println("Completed state handler");
//            testInvoked.invoke();
//        });*/
//
//        assertTrue(stateMachine.getState().contains(CholeskyBlockState.INIT));
//        assertFalse(testInvoked.isInvoked());
//
//        stateMachine.triggerEvent(StateEvent.RECEIVED, L11MatrixDataAvailable.create(Position.fromCoordinates(0, 0), new File("test"), 1));
//        stateMachine.triggerEvent(StateEvent.RECEIVED, L11MatrixDataAvailable.create(Position.fromCoordinates(1, 1), new File("test"), 1));
//        stateMachine.triggerEvent(StateEvent.RECEIVED, L11MatrixDataAvailable.create(Position.fromCoordinates(2, 2), new File("test"), 1));
//
//        stateMachine.triggerEvent(StateEvent.RECEIVED, L21MatrixDataAvailable.create(Position.fromCoordinates(0, 0), new File("test"), 1));
//        stateMachine.triggerEvent(StateEvent.RECEIVED, L21MatrixDataAvailable.create(Position.fromCoordinates(1, 1), new File("test"), 1));
//        stateMachine.triggerEvent(StateEvent.RECEIVED, L21MatrixDataAvailable.create(Position.fromCoordinates(2, 2), new File("test"), 1));
//
//        assertEquals(HandlerState.COMPLETE, stateMachine.getState());
//        assertTrue(testInvoked.isInvoked());
//    }
//
//    void triggerEvent() {
////        StateManagement stateManagement = new StateManagement();
////        stateManagement.triggerEvent(StateEvent.RECEIVED, A11MatrixDataAvailable.create(
////                Position.fromCoordinates(0, 0), BlockMatrixType.A11, new File("test"), 0));
////        stateManagement.onStateChange(BlockState.A22_CALCULATED, () -> {
////
////        });
//
//// state: INIT, COMPETE
////status: true|false
//        // L21Applied.status(): true -- all L21 received and applied; false -- not all
//        // L11.status == init}received|applied|sent
//
////INIT -> WAITING_FOR_L21 -> WAITING_FOR_L11 -> COMPLETE
//
//        /*StateEventHandler<L11MatrixDataAvailable> l11MatrixDataAvailableStateEventHandler = StateEventHandler.newBuilder()
//                .setName("L11")
//                .addExpect(StateEvent.RECEIVED, new L11MatrixDataAvailable(Position.fromCoordinates(a, b)))
//                .setCondition()
//                .build();
//        StateEventHandler<L21MatrixDataAvailable> l21MatrixDataAvailableStateEventHandler = StateEventHandler.newBuilder()
//                .setName("L21")
//                .addExpect(StateEvent.RECEIVED, new L11MatrixDataAvailable(Position.fromCoordinates(a, b)))
//                .setCondition()
//                .build();
//        StateManagement stateManagement = new StateManagement();
//        stateManagement
//                .addHandler(l11MatrixDataAvailableStateEventHandler)
//                .addHandler(l11MatrixDataAvailableStateEventHandler)
//                .addRule("L21", "COMPLETE", "WAITING_FOR_L11")
//                .addRule("L11", "COMPLETE", "COMPLETE")
//                build();*/
//    }
}