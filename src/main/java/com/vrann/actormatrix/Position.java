package com.vrann.actormatrix;

import java.io.Serializable;

public class Position implements Serializable {

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
}
