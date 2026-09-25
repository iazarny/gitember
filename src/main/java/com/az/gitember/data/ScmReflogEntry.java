package com.az.gitember.data;

import java.util.Date;

/**
 * One line from a Git reflog ({@code HEAD}, a branch, or {@code refs/stash}).
 */
public class ScmReflogEntry {

    public static final String KIND_COMMIT = "commit";
    public static final String KIND_RESET = "reset";
    public static final String KIND_CHECKOUT = "checkout";
    public static final String KIND_BRANCH_DELETE = "branch-delete";
    public static final String KIND_STASH = "stash";
    public static final String KIND_OTHER = "other";

    private String refName;
    private String selector;
    private String newId;
    private String oldId;
    private String comment;
    private String whoName;
    private Date when;
    private String kind;

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getNewId() {
        return newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getWhoName() {
        return whoName;
    }

    public void setWhoName(String whoName) {
        this.whoName = whoName;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getShortNewId() {
        return shortSha(newId);
    }

    public String getShortOldId() {
        return shortSha(oldId);
    }

    /**
     * SHA to use when recreating a branch or commit from this line.
     * For checkout, the previous tip ({@code oldId}) is the branch we left.
     */
    public String recoveryCommitId() {
        String sha = newId;
        if (KIND_CHECKOUT.equals(kind) || KIND_RESET.equals(kind) || KIND_BRANCH_DELETE.equals(kind)) {
            if (oldId != null && !isZeroId(oldId)) {
                sha = oldId;
            }
        }
        return sha;
    }

    public String suggestedBranchName() {
        String name = "recovered";
        String c = comment != null ? comment : "";
        String fromCheckout = extractAfter(c, "moving from ", " to ");
        if (fromCheckout != null && !fromCheckout.isBlank()) {
            name = fromCheckout.trim();
        } else {
            String deleted = extractAfter(c, "Deleted from ", null);
            if (deleted == null) {
                deleted = extractAfter(c, "delete branch ", null);
            }
            if (deleted != null && !deleted.isBlank()) {
                name = deleted.trim();
            } else if (newId != null && newId.length() >= 7) {
                name = "recovered-" + newId.substring(0, 7);
            }
        }
        return name;
    }

    public static String classify(String refName, String comment) {
        String kind = KIND_OTHER;
        String c = comment != null ? comment.toLowerCase() : "";
        if (refName != null && (refName.equals("refs/stash") || refName.endsWith("/stash"))) {
            kind = KIND_STASH;
        } else if (c.contains("wip on ") || c.startsWith("on ")) {
            kind = KIND_STASH;
        } else if (c.contains("reset")) {
            kind = KIND_RESET;
        } else if ((c.contains("delete") && c.contains("branch")) || c.contains("deleted from")) {
            kind = KIND_BRANCH_DELETE;
        } else if (c.startsWith("checkout:") || c.contains("moving from")) {
            kind = KIND_CHECKOUT;
        } else if (c.startsWith("commit")) {
            kind = KIND_COMMIT;
        }
        return kind;
    }

    private static String extractAfter(String text, String start, String end) {
        String found = null;
        int from = text.indexOf(start);
        if (from >= 0) {
            int begin = from + start.length();
            if (end != null) {
                int to = text.indexOf(end, begin);
                if (to > begin) {
                    found = text.substring(begin, to);
                }
            } else if (begin < text.length()) {
                found = text.substring(begin).trim();
            }
        }
        return found;
    }

    private static String shortSha(String sha) {
        String shortId = "";
        if (sha != null && sha.length() >= 7 && !isZeroId(sha)) {
            shortId = sha.substring(0, 7);
        }
        return shortId;
    }

    private static boolean isZeroId(String sha) {
        return sha == null || sha.replace("0", "").isEmpty();
    }
}
