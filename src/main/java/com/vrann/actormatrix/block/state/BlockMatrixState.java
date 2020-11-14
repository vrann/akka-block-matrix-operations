package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.*;

import java.util.*;
import java.util.function.Consumer;

public class BlockMatrixState<T extends BlockMatrixType> {

    private BlockState state = BlockStateDefault.INIT;
    private HashMap<T, Set<EventContext>> expected;
    private HashMap<T, HashMap<BlockMatrixStateEvent, List<EventContext>>> received = new HashMap<>();
    private HashMap<T, List<Condition<T>>> conditions;
    private final HashMap<Condition<T>, List<Consumer<BlockMatrixStateEvent>>>
            conditionHandlers;

    private BlockMatrixState(
            HashMap<T, Set<EventContext>> expected,
            HashMap<T, List<Condition<T>>> conditions,
            HashMap<Condition<T>, List<Consumer<BlockMatrixStateEvent>>>
                    conditionHandlers
    ) {
        this.expected = expected;
        this.conditions = conditions;
        this.conditionHandlers = conditionHandlers;
    }

    public void expect(T matrixType, EventContext position) {
        if (!expected.containsKey(matrixType)) {
            expected.put(matrixType, new HashSet<>());
        }
        expected.get(matrixType).add(position);
    }

    public void triggerEvent(BlockMatrixStateEvent event, T matrixType, EventContext eventContext) {
        if (!expected.containsKey(matrixType) || !expected.get(matrixType).contains(eventContext)) {
            throw new RuntimeException(String.format(
                    "Matrix block %s at position %s is not expected",
                    matrixType,
                    eventContext));
        }

        if (!received.containsKey(matrixType)) {
            received.put(matrixType, new HashMap<>());
        }
        if (!received.get(matrixType).containsKey(event)) {
            received.get(matrixType).put(event, new ArrayList<>());
        }
        received.get(matrixType).get(event).add(eventContext);
        //conditions.get(matrixType).receivedEvent(position);

        if (conditions.containsKey(matrixType)) {
            for (Condition<T> condition: conditions.get(matrixType)) {
                condition.evaluate(this);
            }
        }

        for (Condition<T> condition: conditionHandlers.keySet()) {
            if (condition.evaluate(this)) {
                if (conditionHandlers.containsKey(condition)) {
                    conditionHandlers.get(condition).forEach((consumer) -> {
                        consumer.accept(event);
                    });
                }
            }
        }
    }

    public BlockState getState() {
        return state;
    }


    public void setState(BlockState state) {
        this.state = state;
    }


    public static class TransitionRulesBuilder<T extends BlockMatrixType> {

        private Condition<T> lastActiveCondition;
        private HashMap<T, List<Condition<T>>> conditions = new HashMap<>();
        private static TransitionRulesBuilder builderInstance;
        private HashMap<T, Set<EventContext>> expected = new HashMap<>();
        private final HashMap<Condition<T>, List<Consumer<BlockMatrixStateEvent>>>
                conditionHandlers = new HashMap<>();


        private TransitionRulesBuilder() {

        }

        public TransitionRulesBuilder<T> expected(T matrixType, EventContext position) {
            if (!expected.containsKey(matrixType)) {
                expected.put(matrixType, new HashSet<>());
            }
            expected.get(matrixType).add(position);
            return this;
        }

        static <L extends BlockMatrixType> TransitionRulesBuilder<L> newInstance() {
            return new TransitionRulesBuilder<>();
        }

        void addCondition(Condition<T> condition) {
            if (lastActiveCondition != null && !lastActiveCondition.isSetUp()) {
                throw new RuntimeException("Finish setting up condition first");
            }
            lastActiveCondition = condition;
        }

        public TransitionRulesBuilder<T> setState(T matrixType, BlockState newState) {
            lastActiveCondition.onComplete((blockMatrixState) -> blockMatrixState.setState(newState));

            if (lastActiveCondition.isSetUp()) {
                if (!conditions.containsKey(matrixType)) {
                    conditions.put(matrixType, new ArrayList<>());
                }
                conditions.get(matrixType).add(lastActiveCondition);
                lastActiveCondition = null;
            }
            return this;
        }

        public TransitionRulesBuilder<T> onCondition(
              Condition<T> condition, Consumer<BlockMatrixStateEvent> handler
        ) {
            if (!conditionHandlers.containsKey(condition)) {
                conditionHandlers.put(condition, new ArrayList<>());
            }
            conditionHandlers.get(condition).add(handler);
            return this;
        }

        public BlockMatrixState<T> build() {
            return new BlockMatrixState<>(expected, conditions, conditionHandlers);
        }

        public TransitionRulesBuilder<T> when(T matrixType, BlockMatrixStateEvent event) {
            Condition<T> condition = new Condition<>(matrixType, event);
            this.addCondition(condition);
            return this;
        }

        public TransitionRulesBuilder<T> all() {
            lastActiveCondition.all();
            return this;
        }

        public TransitionRulesBuilder<T> one() {
            lastActiveCondition.one();
            return this;
        }
    }


    public static class Condition<T extends BlockMatrixType> {

        private enum ConditionType {
            ALL,
            ONE,
            EACH
        }

        public T getMatrixType() {
            return matrixType;
        }

        public BlockMatrixStateEvent getEvent() {
            return event;
        }

        private T matrixType;
        private BlockMatrixStateEvent event;
        private boolean isSetUp = false;
        private List<Consumer<BlockMatrixState<T>>> handlers = new ArrayList<>();
        private ConditionType currentCondition;

        public Condition(T matrixType, BlockMatrixStateEvent event) {
            this.matrixType = matrixType;
            this.event = event;
        }

        public Condition<T> all() {
            currentCondition = Condition.ConditionType.ALL;
            return this;
        }

        public Condition<T> one() {
            currentCondition = Condition.ConditionType.ONE;
            return this;
        }

        public Condition<T> each() {
            currentCondition = Condition.ConditionType.EACH;
            return this;
        }

        void onComplete(Consumer<BlockMatrixState<T>> handler) {
            handlers.add(handler);
            isSetUp = true;
        }

        boolean isSetUp() {
            return isSetUp;
        }

        boolean evaluate(BlockMatrixState<T> state) {
            Set<EventContext> expectations = state.expected.get(matrixType);
            if (!state.received.containsKey(matrixType)) {
                return false;
            }
            List<EventContext> received = state.received.get(matrixType).get(event);
            if (received == null) {
                return false;
            }
            if (currentCondition == ConditionType.ALL && expectations.size() == received.size()) {
                for (Consumer<BlockMatrixState<T>> handler: handlers) {
                    handler.accept(state);
                }
            } else if ((currentCondition == ConditionType.EACH || currentCondition == ConditionType.ONE) && received.size() >= 1) {
                for (Consumer<BlockMatrixState<T>> handler: handlers) {
                    handler.accept(state);
                }
            }
            return true;
        }
    }

    public static <L extends BlockMatrixType> TransitionRulesBuilder<L> getBuilder()
    {
        TransitionRulesBuilder<L> builder = TransitionRulesBuilder.newInstance();
        return builder;
    }

    public static <L extends BlockMatrixType> TransitionRulesBuilder<L> expected(
            L matrixType, Position position)
    {
        TransitionRulesBuilder<L> builder = getBuilder();
        builder.expected(matrixType, position);
        return builder;
    }
}