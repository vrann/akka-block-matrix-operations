package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.block.BlockMatrixType;
import com.vrann.actormatrix.block.BlockState;
import com.vrann.actormatrix.block.BlockStateDefault;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockMatrixState<T extends BlockMatrixType, E extends BlockMatrixStateEvent> {

    private BlockState state = BlockStateDefault.INIT;
    private HashMap<T, Set<Position>> expected;
    private HashMap<T, HashMap<E, List<Position>>> received = new HashMap<>();
    private HashMap<T, List<Condition<T, E>>> conditions;
    private final HashMap<Condition<T, E>, List<BiConsumer<Condition<T, E>, BlockMatrixState<T, E>>>>
            conditionHandlers;

    private BlockMatrixState(
            HashMap<T, Set<Position>> expected,
            HashMap<T, List<Condition<T, E>>> conditions,
            HashMap<Condition<T, E>, List<BiConsumer<Condition<T, E>, BlockMatrixState<T, E>>>>
                    conditionHandlers
    ) {
        this.expected = expected;
        this.conditions = conditions;
        this.conditionHandlers = conditionHandlers;
    }

    public void triggerEvent(E event, T matrixType, Position position) {
        if (!expected.containsKey(matrixType) || !expected.get(matrixType).contains(position)) {
            throw new RuntimeException(String.format(
                    "Matrix block %s at position %s is not expected",
                    matrixType,
                    position));
        }

        if (!received.containsKey(matrixType)) {
            received.put(matrixType, new HashMap<>());
        }
        if (!received.get(matrixType).containsKey(event)) {
            received.get(matrixType).put(event, new ArrayList<>());
        }
        received.get(matrixType).get(event).add(position);
        //conditions.get(matrixType).receivedEvent(position);

        for (Condition<T, E> condition: conditions.get(matrixType)) {
            condition.evaluate(this);
        }
        for (Condition<T, E> condition: conditionHandlers.keySet()) {
            if (condition.evaluate(this)) {
                if (conditionHandlers.containsKey(condition)) {
                    conditionHandlers.get(condition).forEach((consumer) -> {
                        consumer.accept(condition, this);
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


    public static class TransitionRulesBuilder<T extends BlockMatrixType,  E extends BlockMatrixStateEvent> {

        private Condition<T, E> lastActiveCondition;
        private HashMap<T, List<Condition<T, E>>> conditions = new HashMap<>();
        private static TransitionRulesBuilder builderInstance;
        private HashMap<T, Set<Position>> expected = new HashMap<>();
        private final HashMap<Condition<T, E>, List<BiConsumer<Condition<T, E>, BlockMatrixState<T, E>>>>
                conditionHandlers = new HashMap<>();


        private TransitionRulesBuilder() {

        }

        public TransitionRulesBuilder<T, E> expected(T matrixType, Position position) {
            if (!expected.containsKey(matrixType)) {
                expected.put(matrixType, new HashSet<>());
            }
            expected.get(matrixType).add(position);
            return this;
        }

        static <L extends BlockMatrixType, M extends BlockMatrixStateEvent> TransitionRulesBuilder<L, M> newInstance() {
            return new TransitionRulesBuilder<>();
        }

        void addCondition(Condition<T, E> condition) {
            if (lastActiveCondition != null && !lastActiveCondition.isSetUp()) {
                throw new RuntimeException("Finish setting up condition first");
            }
            lastActiveCondition = condition;
        }

        public TransitionRulesBuilder<T, E> setState(T matrixType, BlockState newState) {
            lastActiveCondition.onComplete((blockMatrixState) -> {
                blockMatrixState.setState(newState);
            });

            if (lastActiveCondition.isSetUp()) {
                if (!conditions.containsKey(matrixType)) {
                    conditions.put(matrixType, new ArrayList<>());
                }
                conditions.get(matrixType).add(lastActiveCondition);
                lastActiveCondition = null;
            }
            return this;
        }

        public TransitionRulesBuilder<T, E> onCondition(
                Condition<T, E> condition, BiConsumer<Condition<T, E>, BlockMatrixState<T, E>> handler
        ) {
            if (!conditionHandlers.containsKey(condition)) {
                conditionHandlers.put(condition, new ArrayList<>());
            }
            conditionHandlers.get(condition).add(handler);
            return this;
        }

        public BlockMatrixState<T, E> build() {
            return new BlockMatrixState<>(expected, conditions, conditionHandlers);
        }

        public TransitionRulesBuilder<T, E> when(T matrixType, E event) {
            Condition<T, E> condition = new Condition<>(matrixType, event);
            this.addCondition(condition);
            return this;
        }

        public TransitionRulesBuilder<T, E> all() {
            lastActiveCondition.all();
            return this;
        }

        public TransitionRulesBuilder<T, E> one() {
            lastActiveCondition.one();
            return this;
        }
    }


    public static class Condition<T extends BlockMatrixType, E extends BlockMatrixStateEvent> {

        private enum ConditionType {
            ALL,
            ONE
        }

        private T matrixType;
        private E event;
        private boolean isSetUp = false;
        private List<Consumer<BlockMatrixState<T, E>>> handlers = new ArrayList<>();
        private ConditionType currentCondition;

        public Condition(T matrixType, E event) {
            this.matrixType = matrixType;
            this.event = event;
        }

        public Condition<T, E> all() {
            currentCondition = Condition.ConditionType.ALL;
            return this;
        }

        public Condition<T, E> one() {
            currentCondition = Condition.ConditionType.ONE;
            return this;
        }

        void onComplete(Consumer<BlockMatrixState<T, E>> handler) {
            handlers.add(handler);
            isSetUp = true;
        }

        boolean isSetUp() {
            return isSetUp;
        }

        boolean evaluate(BlockMatrixState<T, E> state) {
            Set<Position> expectations = state.expected.get(matrixType);
            if (!state.received.containsKey(matrixType)) {
                return false;
            }
            List<Position> received = state.received.get(matrixType).get(event);
            if (received == null) {
                return false;
            }
            if (currentCondition == ConditionType.ALL && expectations.size() == received.size()) {
                for (Consumer<BlockMatrixState<T, E>> handler: handlers) {
                    handler.accept(state);
                }
            } else if (currentCondition == ConditionType.ONE && received.size() >= 1) {
                for (Consumer<BlockMatrixState<T, E>> handler: handlers) {
                    handler.accept(state);
                }
            }
            return true;
        }
    }

    public static <L extends BlockMatrixType, M extends BlockMatrixStateEvent> TransitionRulesBuilder<L, M> expected(
            L matrixType, Position position)
    {
        TransitionRulesBuilder<L, M> builder = TransitionRulesBuilder.newInstance();
        builder.expected(matrixType, position);
        return builder;
    }
}