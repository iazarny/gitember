package com.az.gitember.misc;

import java.io.Serializable;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class Pair<FIRST, SECOND> implements Serializable {

    private static final long serialVersionUID = 2016116L;

    private final FIRST first;
    private final SECOND second;

    public static Pair of(Object first, Object second) {
        return new Pair(first, second);
    }

    public Pair(final FIRST first, final SECOND second) {
        this.first = first;
        this.second = second;
    }

    public FIRST getFirst() {
        return first;
    }

    public SECOND getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!first.equals(pair.first)) return false;
        return second != null ? second.equals(pair.second) : pair.second == null;

    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getName()
                + "{ first: \"" + first + "\","
                + "second: \"" + second + "\" }";
    }

}
