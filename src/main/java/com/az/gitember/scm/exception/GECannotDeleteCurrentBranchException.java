package com.az.gitember.scm.exception;

/**
 * Created by Igor_Azarny on 21.01.2017.
 */
public class GECannotDeleteCurrentBranchException extends Exception {

    public GECannotDeleteCurrentBranchException() {
    }

    public GECannotDeleteCurrentBranchException(String message) {
        super(message);
    }

    public GECannotDeleteCurrentBranchException(String message, Throwable cause) {
        super(message, cause);
    }
}
