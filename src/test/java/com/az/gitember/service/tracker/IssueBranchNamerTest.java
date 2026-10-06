package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueTrackerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IssueBranchNamerTest {

    @Test
    void defaultPattern_typeKeySlug() {
        Issue issue = new Issue("PAY-123", "Fix payment timeout", null, null, null, null);
        String name = IssueBranchNamer.branchName(issue, new IssueTrackerConfig());
        assertEquals("feature/PAY-123-fix-payment-timeout", name);
    }

    @Test
    void customPattern_andType() {
        IssueTrackerConfig cfg = new IssueTrackerConfig();
        cfg.setBranchNamePattern("{key}-{slug}");
        cfg.setBranchType("bugfix");
        Issue issue = new Issue("WEB-7", "Add retry", null, null, null, null);
        assertEquals("WEB-7-add-retry", IssueBranchNamer.branchName(issue, cfg));
    }

    @Test
    void slugify_stripsPunctuationAndTruncates() {
        assertEquals("fix-payment-timeout", IssueBranchNamer.slugify("Fix payment timeout"));
        assertEquals("", IssueBranchNamer.slugify(null));
        String longTitle = "a".repeat(80);
        String slug = IssueBranchNamer.slugify(longTitle);
        assertTrue(slug.length() <= 48);
        assertFalse(slug.endsWith("-"));
    }

    @Test
    void emptyIssue_yieldsEmptyName() {
        assertEquals("", IssueBranchNamer.branchName(Issue.none(), new IssueTrackerConfig()));
        assertEquals("", IssueBranchNamer.branchName(null, new IssueTrackerConfig()));
    }
}
