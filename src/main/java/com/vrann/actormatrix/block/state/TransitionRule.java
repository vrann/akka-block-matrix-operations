package com.vrann.actormatrix.block.state;

import com.vrann.actormatrix.cholesky.CholeskyBlockState;

import java.util.List;

public class TransitionRule {

    private final CholeskyBlockState from;
    private final CholeskyBlockState nextState;
    private final List<StateCondition> conditions;

    public TransitionRule(CholeskyBlockState from, CholeskyBlockState nextState, List<StateCondition> conditions) {
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

    public CholeskyBlockState nextState() {
        return nextState;
    }

    public CholeskyBlockState from() {
        return from;
    }

}
