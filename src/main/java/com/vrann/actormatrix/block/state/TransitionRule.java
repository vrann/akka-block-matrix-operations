package com.vrann.actormatrix.block.state;

import java.util.List;
import java.util.concurrent.locks.Condition;

public class TransitionRule {

    private final BlockState from;
    private final BlockState nextState;
    private final List<StateCondition> conditions;

    public TransitionRule(BlockState from, BlockState nextState, List<StateCondition> conditions) {
        this.from = from;
        this.nextState = nextState;
        this.conditions = conditions;
    }

    public boolean met() {
        for (StateCondition condition: conditions) {
            //if (condition.)
        }
        return false;
    }

    public BlockState nextState() {
        return nextState;
    }

    public BlockState from() {
        return from;
    }

}
