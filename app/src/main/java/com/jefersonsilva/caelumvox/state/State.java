package com.jefersonsilva.caelumvox.state;

import java.io.Serializable;

/**
 * Created by jefrsilva on 27/12/16.
 */

public abstract class State implements Serializable {

    public static final State IDLE = new IdleState();

    public abstract void update(double amplitude);

    public abstract String getName();

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof State) {
            State state = (State) obj;
            return state.getName().equals(getName());
        }
        return false;
    }
}
