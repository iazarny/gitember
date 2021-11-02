package com.az.gitember.data;

public enum Side {
    A,
    B;

    public Side opposite() {
        if (this == A) {
            return B;
        }
        return A;
    }
}
