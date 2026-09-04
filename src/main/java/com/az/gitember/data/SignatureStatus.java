package com.az.gitember.data;

/**
 * Presence and, after an on-demand check, cryptographic outcome of a commit or tag signature.
 *
 * <p>{@link #UNSIGNED} / {@link #SIGNED} are filled while adapting a {@code RevCommit}
 * (signature bytes present or not). {@link #VERIFIED}, {@link #INVALID} and {@link #UNKNOWN}
 * are set only when the user asks JGit's {@code SignatureVerifier} to check the signature.
 */
public enum SignatureStatus {
    UNSIGNED,
    SIGNED,
    VERIFIED,
    INVALID,
    UNKNOWN;

    public boolean isPresent() {
        return this != UNSIGNED;
    }
}
