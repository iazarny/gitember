package com.az.gitember.data;

import com.az.gitember.service.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ScmItem extends Pair<String, ScmItemAttribute> implements Comparable<ScmItem> {

    public enum  BODY_TYPE {
        WORK_SPACE,
        COMMIT_VERION,
        RAW_DIFF;
    }

    public interface Status {

        String CONFLICT = "Conflict";

        String CONFLICT_BOTH_DELETED = "Conflict_CBD"; //Exists in base, but neither in ours nor in theirs.
        String CONFLICT_DELETED_BY_US = "Conflict_DBU"; // Exists in base and theirs, but not in ours.
        String CONFLICT_DELETED_BY_THEM = "Conflict_DBT"; //Exists in base and ours, but no in theirs.

        String CONFLICT_ADDED_BY_US = "Conflict_ABU"; //Only exists in ours.
        String CONFLICT_ADDED_BY_THEM = "Conflict_ABT"; // Only exists in theirs.
        String CONFLICT_BOTH_ADDED = "Conflict_BA"; // Exists in ours and theirs, but not in base.
        String CONFLICT_BOTH_MODIFIED = "Conflict_BM"; //Exists in all stages, content conflict.


        String ADDED = "Added";
        String MISSED = "Missed";
        String MODIFIED = "Modified";
        String CHANGED = "Changed";
        String REMOVED = "Removed";
        String UNCOMMITED = "Uncommitted";
        String UNTRACKED = "Untracked";
        String UNTRACKED_FOLDER = "UntrackedFolder";

        String RENAMED = "Renamed";
    }


    private static Map<String, Integer> sortOrder = new HashMap<>();

    static {
        sortOrder.put(Status.ADDED, 8);
        sortOrder.put(Status.RENAMED, 7);
        sortOrder.put(Status.CHANGED, 6);
        sortOrder.put(Status.REMOVED, 5);
        sortOrder.put(Status.CONFLICT, 4);

        sortOrder.put(Status.MODIFIED, 3);
        sortOrder.put(Status.MISSED, 2);
        sortOrder.put(Status.UNTRACKED, 1);
        sortOrder.put(Status.UNTRACKED_FOLDER, 0);
    }

    private String commitName;

    public ScmItem(String s, ScmItemAttribute attribute, String commitName) {
        super(s, attribute);
        this.commitName = commitName;
    }

    public ScmItem(String s, ScmItemAttribute attribute) {
        this(s, attribute, null);
    }

    public String getShortName() {
        return super.getFirst();
    }

    public ScmItemAttribute getAttribute() {
        return super.getSecond();
    }

    public String getCommitName() {
        return commitName;
    }

    public void setCommitName(String commitName) {
        this.commitName = commitName;
    }

    public String getViewRepresentation() {
        final String val;
        if (getAttribute().getStatus() != null && getAttribute().getStatus().equals(ScmItem.Status.RENAMED)) {
            val = getAttribute().getOldName() + " -> " + getShortName();
        } else {
            val = getShortName();
        }
        return val;
    }

    @Override
    public int compareTo(ScmItem o) {
        int ret = sortOrder.getOrDefault(o.getAttribute().getStatus(), 0)
                - sortOrder.getOrDefault(this.getAttribute().getStatus(), 0);
        if (ret == 0) {
            ret = this.getShortName().compareTo(o.getShortName());

        }
        return ret;
    }

    /**
     * Get file info.
     * @param bodyType type
     * @return mimetype / path to file.
     * @throws IOException
     */
    public Path getFilePath(ScmItem.BODY_TYPE bodyType) throws IOException {
        switch (bodyType) {
            case WORK_SPACE: {
                return Path.of(Context.getProjectFolder(), getShortName());
            }
            case COMMIT_VERION: {
                String saved = Context.getGitRepoService().saveFile(getCommitName(), getShortName());
                return Path.of(saved);
            }
            default: {
                throw new IOException("Given body type " + bodyType + " is not supported ");
            }
        }
    }

    public byte[] getBody(ScmItem.BODY_TYPE bodyType) throws IOException {
        switch (bodyType) {
            case WORK_SPACE: {
                final Path path = Path.of(Context.getProjectFolder(), getShortName());
                final String mimeType = Files.probeContentType(path);
                return Files.readAllBytes(path);
            }
            case COMMIT_VERION: {
                String saved = Context.getGitRepoService().saveFile(getCommitName(), getShortName());
                final Path path = Path.of(saved);
                final String mimeType = Files.probeContentType(path);
                return Files.readAllBytes(path);
            }

            case RAW_DIFF: {
                return Context.getGitRepoService().getRawDiff( getCommitName(), getShortName()).getBytes();
            }
        }

        return new byte[0];

    }




}
