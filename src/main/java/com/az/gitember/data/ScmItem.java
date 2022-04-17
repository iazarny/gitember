package com.az.gitember.data;

import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.az.gitember.service.GitemberUtil.is;

public class ScmItem extends Pair<String, ScmItemAttribute> implements Comparable<ScmItem> {

    public enum  BODY_TYPE {
        WORK_SPACE,
        COMMIT_VERSION,
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

        String LFS_POINTER = "lfs_pointer";
        String LFS_FILE = "lfs_file";
        String LFS = "Lfs";
    }


    //TODO eliminate this class
    public static class Changes {
        private String commitInfo;
        private String commitAuthor;
        private String commitEmail;
        private Date date;

        public  Changes(final RevCommit revCommit) {
            this.setDate(GitemberUtil.intToDate(revCommit.getCommitTime()));
            this.setCommitAuthor(revCommit.getAuthorIdent().getName());
            this.setCommitEmail(revCommit.getAuthorIdent().getEmailAddress());
            this.setCommitInfo(revCommit.getShortMessage());
        }

        public String getCommitInfo() {
            return commitInfo;
        }

        public void setCommitInfo(String commitInfo) {
            this.commitInfo = commitInfo;
        }

        public String getCommitAuthor() {
            return commitAuthor;
        }

        public void setCommitAuthor(String commitAuthor) {
            this.commitAuthor = commitAuthor;
        }

        public String getCommitEmail() {
            return commitEmail;
        }

        public void setCommitEmail(String commitEmail) {
            this.commitEmail = commitEmail;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
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

    private RevCommit revCommit;
    private Changes changes;

    public ScmItem(String s, ScmItemAttribute attribute, RevCommit revCommit) {
        super(s, attribute);
        this.revCommit = revCommit;
    }

    public ScmItem(String s, ScmItemAttribute attribute) {
        this(s, attribute, null);
    }

    public Changes getChanges() {
        return changes;
    }

    public void setChanges(Changes changes) {
        this.changes = changes;
    }

    public String getShortName() {
        return super.getFirst();
    }

    public ScmItemAttribute getAttribute() {
        return super.getSecond();
    }

    public String getCommitName() {
        return revCommit.getName();
    }

    public void setCommitName(RevCommit revCommit) {
        this.revCommit = revCommit;
    }

    public RevCommit getRevCommit() {
        return revCommit;
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

    @Override
    public boolean equals(Object o) {
        return compareTo((ScmItem) o)== 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), revCommit);
    }

    public ScmItem withChanges(final RevCommit revCommit) {
        if (revCommit != null) {
            this.setChanges(new Changes(revCommit));
        }
        return this;
    }

    public String getChangeNameSafe() {
        if (getChanges() != null) {
            return getChanges().getCommitInfo();
        }
        return "";
    }

    public String getChangeDateSafe() {
        if (getChanges() != null) {
            return GitemberUtil.formatDate(getChanges().getDate());
        }
        return "";
    }

    public String getChangeAuthorSafe() {
        if (getChanges() != null) {
            return getChanges().getCommitAuthor() + " " + getChanges().getCommitEmail();
        }
        return "";
    }

    /**
     * Get file path.
     * @param bodyType type
     * @return mimetype / path to file.
     * @throws IOException
     */
    public Path getFilePath(ScmItem.BODY_TYPE bodyType) throws IOException {
        switch (bodyType) {
            case WORK_SPACE: {
                return Path.of(Context.getProjectFolder(), getShortName());
            }
            case COMMIT_VERSION: {
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
                return Files.readAllBytes(path);
            }
            case COMMIT_VERSION: {
                String saved = Context.getGitRepoService().saveFile(getCommitName(), getShortName());
                final Path path = Path.of(saved);
                return Files.readAllBytes(path);
            }

            case RAW_DIFF: {
                return Context.getGitRepoService().getRawDiff( getCommitName(), getShortName()).getBytes();
            }
        }

        return new byte[0];

    }

    /**
     * @return null in case of unchanged lfs
     */
    public Integer staged() {
        if (ScmItem.Status.LFS.equals(getAttribute().getStatus())) {
            return -1;
        } else if (is(getAttribute().getStatus()).oneOf(
                ScmItem.Status.MODIFIED, ScmItem.Status.MISSED,
                ScmItem.Status.CONFLICT, ScmItem.Status.UNTRACKED)) {
            return 0;
        }
        return 1;
    }

    @Override
    public String toString() {
        return getShortName() + " staged " + staged() + "  status " + getAttribute();
    }
}
