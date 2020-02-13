package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.Message;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.message.BlockMatrixMessage;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StateEventHandler<T extends BlockMatrixMessage> {
    private final List<Consumer<HandlerState>> completionListeners = new ArrayList<>();
    private HandlerState currentState = HandlerState.INIT;
    private final Map<StateEvent, HashSet<Message>> expected;
    private final Map<StateEvent, LinkedList<Message>> triggered = new HashMap<>();
    private final String name;
    private final Set<MessageEvent> expectedMessages;
    private final Set<MessageEvent> receivedMessages = new HashSet<>();
    private final Map<HandlerState, List<Consumer<HandlerState>>> listeners = new HashMap<>();
    private final List<BiConsumer<String, HandlerState>> handlerStateChangeListeners = new ArrayList<>();

    private StateEventHandler(String name, Map<StateEvent, HashSet<Message>> expected, Set<MessageEvent> expectedMessages) {
        this.expected = expected;
        this.name = name;
        this.expectedMessages = expectedMessages;
        evaluateState();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(name).append(" State Handler: ").append(currentState).append(". Expected messages:\n");
        for (StateEvent event: expected.keySet()) {
            result.append(event).append(":\n");
            for (Message m: expected.get(event)) {
                result.append("\t").append(m).append("\n");
            }
        }
        result.append("Expected:\n");
        for (MessageEvent expectedMessage: expectedMessages) {
            result.append(expectedMessage).append("\n");
        }
        result.append("Received:\n");
        for (MessageEvent receivedMessage: receivedMessages) {
            result.append(receivedMessage).append("\n");
        }
        return result.toString();
    }

    public void triggerEvent(StateEvent event, T message) {
        if (expected.containsKey(event) && expected.get(event).contains(message)) {
            expected.get(event).remove(message);
            if (expected.get(event).size() == 0) {
                expected.remove(event);
            }
            if (!triggered.containsKey(event)) {
                triggered.put(event, new LinkedList<>());
            }
            triggered.get(event).add(message);
            evaluateState();
        }
        MessageEvent messageEvent = new MessageEvent(event, message.getClass(), message.getPosition());
        if (expectedMessages.contains(messageEvent)) {
            expectedMessages.remove(messageEvent);
            receivedMessages.add(messageEvent);
        }
        System.out.println(this);
    }

    private void evaluateState() {
        /*for (TransitionRule transitionRule: transitionRules.get(currentState)) {
            if (transitionRule.met()) {
                state = transitionRule.nextState();
            }
        }*/
        HandlerState newState = HandlerState.COMPLETE;
        handlerStateChangeListeners.forEach(f -> f.accept(newState));
        for (Consumer<HandlerState> listener: listeners.get(newState)) {
            listener.accept(newState);
        }
        if (expected.size() == 0) {
            //nothing is expected anymore
            currentState = HandlerState.COMPLETE;
            triggerComplete();
        }
    }

    private void triggerComplete() {
        for (Consumer<HandlerState> f: completionListeners) {
            f.accept(HandlerState.COMPLETE);
        }
    }

    public void onComplete(Consumer<HandlerState> consumer) {
        completionListeners.add(consumer);
    }

    public void onStateChange(BiConsumer<String, HandlerState> f) {
        handlerStateChangeListeners.add(f);
    }

    public void onState(HandlerState expectedState, Consumer<HandlerState> f) {
        if (!listeners.containsKey(expectedState)) {
            listeners.put(expectedState, new ArrayList<>());
        }
        listeners.get(expectedState).add(f);
    }

    static class MessageEvent {
        private final StateEvent eventName;
        private final Class<? extends Message> messageClass;
        private final Position position;

        MessageEvent(StateEvent eventName, Class<? extends Message> messageClass, Position position) {
            this.eventName = eventName;
            this.messageClass = messageClass;
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageEvent that = (MessageEvent) o;
            return eventName == that.eventName &&
                    Objects.equals(messageClass, that.messageClass) &&
                    Objects.equals(position, that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventName, messageClass, position);
        }

        @Override
        public String toString() {
            return (new StringBuilder())
                    .append(eventName).append("-")
                    .append(messageClass).append("-")
                    .append(position)
                    .toString();
        }
    }

    static class Builder<M extends BlockMatrixMessage> {

        private final Map<StateEvent, HashSet<Message>> expected = new HashMap<>();
        private final Set<MessageEvent> expectedMessages = new HashSet<>();
        private String name;

        public Builder<M> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<M> setCondition(StateEvent eventType, StateCondition condition, HandlerState nextState) {
            return this;
        }

        public Builder<M> addExpecMessage(StateEvent event, Class<? extends Message> message, Position position) {
            expectedMessages.add(new MessageEvent(event, message, position));
            return this;
        }


        public Builder<M> addExpect(StateEvent event, Message message) {
            if (!expected.containsKey(event)) {
                HashSet<Message> expectedMessage = new HashSet<>();
                expectedMessage.add(message);
                expected.put(event, expectedMessage);
            } else {
                expected.get(event).add(message);
            }
            return this;
        }

        public StateEventHandler<M> build() {
            return new StateEventHandler<>(name, expected, expectedMessages);
        }
    }

    public HandlerState getState() {
        return currentState;
    }

    static public <L extends  BlockMatrixMessage> Builder<L> newBuilder() {
        return new Builder<>();
    }
}
