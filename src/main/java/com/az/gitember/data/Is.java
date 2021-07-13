package com.az.gitember.data;

public class Is<T> {

    private final T value;

    public static Is value(Object value) {
        return new Is(value);
    }

    public static Is<String> string(String value) {
        return new Is<String>(value);
    }

    public Is(T value) {
        if (value == null) {
            throw new IllegalArgumentException("The value must be not null");
        }
        this.value = value;
    }

    public boolean in(T ... variants) {
        for (T v : variants) {
            if (value.equals(v)) {
                return true;
            }
        }
        return false;
    }

}
