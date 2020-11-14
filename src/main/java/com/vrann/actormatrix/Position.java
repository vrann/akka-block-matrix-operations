package com.vrann.actormatrix;

import com.vrann.actormatrix.block.EventContext;
import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable, EventContext {

    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(String x, String y) {
        this.x = Integer.valueOf(x);
        this.y = Integer.valueOf(y);
    }

    public static Position fromCoordinates(int x, int y) {
        return new Position(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY()
    {
        return y;
    }

    @Override
    public String toString() {
        return (new StringBuilder()).append(Integer.toString(x)).append(':').append(Integer.toString(y)).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x &&
                y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
