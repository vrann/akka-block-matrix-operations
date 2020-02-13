package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.block.Block;
import com.vrann.actormatrix.cholesky.message.BlockMatrixMessage;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public /**
 *
 * initialize state machine --> initialize handlers --> attach callbacks
 * example of callbacks: when all L21 received and applied by subdiagonal block && l11 received -> apply l11 and send l21. When l21 send finish execution.
 *      Can l11 be applied before l21 received? defenitely we cannot send l21 right
 * event ---> state machine ---> match handle by event --> recalculate handler state --> trigger callbacks ---> recalculate state --> trigger callbacks
 *
 *
 */

class StateManagement {

    private final Map<StateEvent, StateEventHandler<BlockMatrixMessage>> handlers = new HashMap<>();
    private final List<Consumer<EnumSet<BlockState>>> stateChangeListeners = new ArrayList<>();
    private final List<Consumer<BlockState>> completionListeners = new ArrayList<>();
    private EnumSet<BlockState> currentState;
    private final Map<String, StateEventHandler<?>> stateEventHandlers;

    private StateManagement(Map<String, StateEventHandler<?>> stateEventHandlers) {
        this.stateEventHandlers = stateEventHandlers;
    }

    public void triggerEvent(StateEvent event, BlockMatrixMessage message) {
        handlers.get(event).triggerEvent(event, message);
    }

    public void evaluateState(String handlerName, HandlerState state) {
        boolean stateChanged = false;
        switch (handlerName) {
            case "L11":
                if (state.equals(HandlerState.RECEIVED)) {
                    currentState.add(BlockState.L11_RECEIVED);
                    stateChanged = true;
                } else if (state.equals(HandlerState.COMPLETE)) {
                    currentState.add(BlockState.L11_CALCULATED);
                    stateChanged = true;
                }
                break;
            case "L21":
                if (state.equals(HandlerState.RECEIVED)) {
                    currentState.add(BlockState.L21_ALL_RECEIVED);
                    stateChanged = true;
                } else if (state.equals(HandlerState.COMPLETE)) {
                    currentState.add(BlockState.L21_CALCULATED);
                    stateChanged = true;
                }
                break;
        }
        if (stateChanged) {
            stateChangeListeners.forEach(listener -> listener.accept(currentState));
        }
    }

    /*public void setStateTransitionRules() {

        BlockState.INIT, CholeskyMatrixHandlerState.L21,  HandlerState.RECEIVED, BlockState.L21_ALL_RECEIVED
        BlockState.L21_ALL_RECEIVED, CholeskyMatrixHandlerState.L21,  HandlerState.COMPLETE, BlockState.L21_CALCULATED
        BlockState.INIT, CholeskyMatrixHandlerState.L11,  HandlerState.RECEIVED, BlockState.L11_RECEIVED
                (BlockState.L11_RECEIVED, BlockState.L21_CALCULATED), CholeskyMatrixHandlerState.L11,  HandlerState.COMPLETE, BlockState.L11_CALCULATED
    }*/

    public void onStateChange(Consumer<EnumSet<BlockState>> f) {
        stateChangeListeners.add(f);
    }

    public void onComplete(Consumer<BlockState> consumer) {
        completionListeners.add(consumer);
    }

    void registerEventHandler(StateEvent event, StateEventHandler handler) {
        handler.onStateChange(this::evaluateState);
        handlers.put(event, handler);
    }

    private Consumer<HandlerState> processHandlerCompletion() {
        return (h) -> {};
    }

//    private void changeState(BlockState newState) {
//        for (BiConsumer<BlockState, BlockState> f: stateChangeListeners) {
//            f.accept(currentState, newState);
//        }
//    }

    private void complete() {
        for (Consumer listener: completionListeners) {
            listener.accept(BlockState.COMPLETE);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

//    public void changeStateOnStateHandler(String name, HandlerState expectedState, BlockState newState) {
//        stateEventHandlers.get(name).onState(expectedState, () -> this.changeState(newState));
//    }

    public static class Builder {

        private final Map<String, StateEventHandler<?>> stateEventHandlers = new HashMap<>();

        public Builder addSateHandler(StateEventHandler<?> eventHandler) {
            stateEventHandlers.put(eventHandler.getName(), eventHandler);
            return this;
        }

        public StateManagement build() {
            return new StateManagement(stateEventHandlers);
        }
    }
}
