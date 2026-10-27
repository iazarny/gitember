package com.az.gitember.data;

/**
 * A Git LFS file lock as returned by the locking API.
 */
public class LfsLock {

    private final String id;
    private final String path;
    private final String owner;
    private final String lockedAt;

    public LfsLock(String id, String path, String owner, String lockedAt) {
        this.id = id;
        this.path = path;
        this.owner = owner;
        this.lockedAt = lockedAt;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getOwner() {
        return owner;
    }

    public String getLockedAt() {
        return lockedAt;
    }

    @Override
    public String toString() {
        return path + " locked by " + owner;
    }
}
