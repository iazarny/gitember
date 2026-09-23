package com.az.gitember.data;

import java.io.IOException;

/**
 * Typed failure from a Git LFS transfer or lock operation.
 */
public class LfsException extends IOException {

    public enum Kind {
        NO_REMOTE,
        AUTH,
        NETWORK,
        SERVER,
        LOCKED,
        NOT_LOCKED,
        POINTER,
        OTHER
    }

    private final Kind kind;

    public LfsException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public LfsException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }
}
