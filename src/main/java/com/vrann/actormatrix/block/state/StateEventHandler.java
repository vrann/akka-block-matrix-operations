package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.Message;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.message.BlockMatrixMessage;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StateEventHandler<T extends Enum> {
    private final List<Consumer<HandlerState>> completionListeners = new ArrayList<>();
    private HandlerState currentState = HandlerState.INIT;
    private final Map<T, HashSet<Message>> expected;
    private final Map<T, LinkedList<Message>> triggered = new HashMap<>();
    private final String name;
    private final HashMap<T, HashSet<MessageEvent<T>>> expectedMessages;
    private final Set<MessageEvent> receivedMessages = new HashSet<>();
    private final Map<HandlerState, List<Consumer<HandlerState>>> listeners = new HashMap<>();
    private final List<BiConsumer<String, HandlerState>> handlerStateChangeListeners = new ArrayList<>();
    private final Class<T> eventType;

    private StateEventHandler(Class<T> eventType, String name, Map<T, HashSet<Message>> expected, HashMap<T, HashSet<MessageEvent<T>>> expectedMessages) {
        this.expected = expected;
        this.name = name;
        this.expectedMessages = expectedMessages;
        this.eventType = eventType;
        evaluateState();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(name).append(" State Handler: ").append(currentState).append(". Expected messages:\n");
        for (T event: expected.keySet()) {
            result.append(event).append(":\n");
            for (Message m: expected.get(event)) {
                result.append("\t").append(m).append("\n");
            }
        }



        for (T enumValue: this.eventType.getEnumConstants()) {
            result.append("Expected").append(enumValue).append("\n");
            for (MessageEvent expectedMessage : expectedMessages.get(enumValue)) {
                result.append(expectedMessage).append("\n");
            }
        }
        /*result.append("Received:\n");
        for (MessageEvent receivedMessage: receivedMessages) {
            result.append(receivedMessage).append("\n");
        }*/
        return result.toString();
    }

    public void triggerEvent(T event, BlockMatrixMessage message) {
        /*if (expected.containsKey(event) && expected.get(event).contains(message)) {
            expected.get(event).remove(message);
            if (expected.get(event).size() == 0) {
                expected.remove(event);
            }
            if (!triggered.containsKey(event)) {
                triggered.put(event, new LinkedList<>());
            }
            triggered.get(event).add(message);
            evaluateState();
        }*/
        MessageEvent messageEvent = new MessageEvent<>(event, message.getClass(), message.getPosition());
        if (expectedMessages.containsKey(event)) {
            expectedMessages.get(event).remove(messageEvent);
            receivedMessages.add(messageEvent);
        }
        System.out.println(this);
    }

    private void evaluateState() {
        /*for (TransitionRule transitionRule: transitionRules.get(currentState)) {
            if (transitionRule.met()) {
                state = transitionRule.nextState();
            }
        }
        HandlerState newState = HandlerState.COMPLETE;
        handlerStateChangeListeners.forEach(f -> f.accept(name, newState));
        if (listeners.containsKey(newState)) {
            for (Consumer<HandlerState> listener : listeners.get(newState)) {
                listener.accept(newState);
            }
        }*/
        HandlerState oldState = currentState;
        if (expectedMessages.size() == 0) {
            //nothing is expected anymore
            currentState = HandlerState.COMPLETE;
            triggerComplete();
        }
        if (!oldState.equals(currentState)) {
            handlerStateChangeListeners.forEach(f -> f.accept(name, currentState));
        }
        if (currentState.equals(HandlerState.COMPLETE)) {
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

    static class MessageEvent<T extends Enum> {
        private final T eventName;
        private final Class<? extends Message> messageClass;
        private final Position position;

        MessageEvent(T eventName, Class<? extends Message> messageClass, Position position) {
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

    public static class Builder<K extends Enum> {

        private Builder(Class<K> eventType) {
            this.eventType = eventType;
        }

//        class EventBuilder {
//
//            private K event;
//
//            public EventBuilder (K event) {
//                this.event = event;
//            }
//
//            public Builder evaluateState(Consumer<StateEventHandler<?>> attachFunction) {
//                Builder.this.addStateEvaluateHandler(event, attachFunction);
//                return Builder.this;
//            }
//        }
//
//        private void addStateEvaluateHandler(K event, Consumer<StateEventHandler<?>> attachFunction) {
//
//            evaluateStateHandler.
//        }

        private final Map<K, HashSet<Message>> expected = new HashMap<>();
        private final HashMap<K, HashSet<MessageEvent<K>>> expectedMessages = new HashMap<>();
        private String name;
        private final Class<K> eventType;
        HashMap<K, List<Consumer<StateEventHandler<?>>>> evaluateStateHandler = new HashMap<>();

        public Builder<K> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<K> setCondition(K event, StateCondition condition, HandlerState nextState) {
            return this;
        }

        public Builder<K> addExpectMessage(K event, Class<? extends Message> message, Position position) {
            if (!expectedMessages.containsKey(event)) {
                expectedMessages.put(event, new HashSet<>());
            }
            expectedMessages.get(event).add(new MessageEvent<>(event, message, position));
            return this;
        }


        public Builder<K> addExpect(K event, Message message) {
            if (!expected.containsKey(event)) {
                HashSet<Message> expectedMessage = new HashSet<>();
                expectedMessage.add(message);
                expected.put(event, expectedMessage);
            } else {
                expected.get(event).add(message);
            }
            return this;
        }

        public StateEventHandler<K> build() {
            return new  StateEventHandler<K>(eventType, name, expected, expectedMessages);
        }

//        public EventBuilder onEvent(K received) {
//            return new EventBuilder(received);
//        }
    }

    public HandlerState getState() {
        return currentState;
    }

    static public <L extends  Enum> Builder<L> newBuilder(Class<L> eventType) {
        return new Builder<>(eventType);
    }
}
