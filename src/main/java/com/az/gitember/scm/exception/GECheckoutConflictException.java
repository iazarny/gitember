package com.az.gitember.scm.exception;

import java.util.List;

/**
 * Created by Igor_Azarny on 18.01.2017.
 */
public class GECheckoutConflictException extends Exception {

    private final List<String> conflicting;

    public GECheckoutConflictException(String message, List<String> conflicting) {
        super(message);
        this.conflicting = conflicting;
    }

    public List<String> getConflicting() {
        return conflicting;
    }
}
