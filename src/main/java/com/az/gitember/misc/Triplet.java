package com.az.gitember.misc;

import java.io.Serializable;

/**
 * Created by Igor_Azarny on 11 - Jan - 2017.
 */
public class Triplet<FIRST, SECOND, THIRD> implements Serializable {

    private static final long serialVersionUID = 2016116L;

    private FIRST first;
    private SECOND second;
    private THIRD third;

    public Triplet(FIRST first, SECOND second, THIRD third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Triplet() {
    }


    public static Triplet of(Object first, Object second, Object thrird) {
        return new Triplet(first, second, thrird);
    }


    public FIRST getFirst() {
        return first;
    }

    public SECOND getSecond() {
        return second;
    }

    public THIRD getThird() {
        return third;
    }

    public void setFirst(FIRST first) {
        this.first = first;
    }

    public void setSecond(SECOND second) {
        this.second = second;
    }

    public void setThird(THIRD third) {
        this.third = third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;

        if (first != null ? !first.equals(triplet.first) : triplet.first != null) return false;
        if (second != null ? !second.equals(triplet.second) : triplet.second != null) return false;
        return third != null ? third.equals(triplet.third) : triplet.third == null;

    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }
}
