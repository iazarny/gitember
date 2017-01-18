package com.az.gitember.scm.exception;

/**
 * Created by Igor_Azarny on 18.01.2017.
 */
public class GEScmAPIException extends Exception {

    public GEScmAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public GEScmAPIException() {
    }

    public GEScmAPIException(String message) {
        super(message);
    }
}
