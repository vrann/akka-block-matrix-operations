package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.cholesky.CholeskyBlockState;
import com.vrann.actormatrix.cholesky.message.BlockMatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.BlockMatrixMessage;

import java.util.*;
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

    private final Map<StateEvent, StateEventHandler<?>> handlers = new HashMap<>();
    private final List<Consumer<EnumSet<CholeskyBlockState>>> stateChangeListeners = new ArrayList<>();
    private final List<Consumer<CholeskyBlockState>> completionListeners = new ArrayList<>();
    private EnumSet<CholeskyBlockState> currentState = EnumSet.of(CholeskyBlockState.INIT);
    private final Map<Class<? extends BlockMatrixMessage>, StateEventHandler<?>> stateEventHandlers;

    private StateManagement(Map<Class<? extends BlockMatrixMessage>, StateEventHandler<?>> stateEventHandlers) {
        this.stateEventHandlers = stateEventHandlers;
    }

    public <T extends Enum> void triggerEvent(T event, T message) {
        if (stateEventHandlers.containsKey(message.getClass())) {
//            ((StateEventHandler<T>)stateEventHandlers.get(message.getClass())).triggerEvent(event, message);
        }
    }

    public EnumSet<CholeskyBlockState> getState() {
        return currentState;
    }

    private void evaluateState(String handlerName, HandlerState state) {
        boolean stateChanged = false;
        switch (handlerName) {
            case "L11":
                if (state.equals(HandlerState.RECEIVED)) {
                    currentState.add(CholeskyBlockState.L11_RECEIVED);
                    stateChanged = true;
                } else if (state.equals(HandlerState.COMPLETE)) {
                    currentState.add(CholeskyBlockState.L11_CALCULATED);
                    stateChanged = true;
                }
                break;
            case "L21":
                if (state.equals(HandlerState.RECEIVED)) {
                    currentState.add(CholeskyBlockState.L21_ALL_RECEIVED);
                    stateChanged = true;
                } else if (state.equals(HandlerState.COMPLETE)) {
                    currentState.add(CholeskyBlockState.L21_CALCULATED);
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

    public void onStateChange(Consumer<EnumSet<CholeskyBlockState>> f) {
        stateChangeListeners.add(f);
    }

    public void onComplete(Consumer<CholeskyBlockState> consumer) {
        completionListeners.add(consumer);
    }

    private void registerEventHandler(StateEvent event, StateEventHandler<Enum> handler) {
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
        for (Consumer<CholeskyBlockState> listener : completionListeners) {
            listener.accept(CholeskyBlockState.COMPLETE);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

//    public void changeStateOnStateHandler(String name, HandlerState expectedState, BlockState newState) {
//        stateEventHandlers.get(name).onState(expectedState, () -> this.changeState(newState));
//    }

    public static class Builder {

        private final Map<Class<? extends BlockMatrixMessage>, StateEventHandler<? extends Enum>> stateEventHandlers = new HashMap<>();

        public <T extends BlockMatrixMessage> Builder addSateHandler(Class<T> type, StateEventHandler<? extends Enum> eventHandler) {
            stateEventHandlers.put(type, eventHandler);
            return this;
        }

        public StateManagement build() {
            return new StateManagement(stateEventHandlers);
        }
    }
}
