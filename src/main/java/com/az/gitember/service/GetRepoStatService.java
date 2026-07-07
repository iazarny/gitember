package com.az.gitember.service;

import com.az.gitember.data.Const;
import com.az.gitember.data.RepoStats;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmItem;

import java.io.File;
import java.util.Date;
import java.util.List;

public class GetRepoStatService {

    /**
     * Reads branch, ahead/behind, working-copy changes and last-fetch time for one repository.
     * @return RepoStats
     * */
    public RepoStats computeStats(String projectHome) throws Exception {
        try (GitRepoService svc = GitRepoService.of(projectHome)){
            String branch = "";
            int ahead = 0;
            int behind = 0;
            for (ScmBranch b : svc.getBranches()) {
                if (b.isHead()) {
                    branch = b.getShortName();
                    ahead = b.getAheadCount();
                    behind = b.getBehindCount();
                    break;
                }
            }

            int modified = 0;
            int conflicts = 0;
            List<ScmItem> items = svc.getStatuses(null);
            if (items != null) {
                for (ScmItem item : items) {
                    String status = item.getAttribute() != null ? item.getAttribute().getStatus() : null;
                    if (status != null && status.startsWith(ScmItem.Status.CONFLICT)) {
                        conflicts++;
                    } else {
                        modified++;
                    }
                }
            }
            return new RepoStats(branch, modified, conflicts, ahead, behind, fetchTime(projectHome), false);
        }
    }

    /** Last successful fetch, approximated by the {@code .git/FETCH_HEAD} modification time. */
    private static Date fetchTime(String home) {
        File fetchHead = new File(home + File.separator + Const.GIT_FOLDER, "FETCH_HEAD");
        if (fetchHead.isFile()) {
            long modified = fetchHead.lastModified();
            if (modified > 0) return new Date(modified);
        }
        return null;
    }


}
