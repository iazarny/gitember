package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IssueCommitMessageTest {

    @Test
    void prefixesKey_whenMissing() {
        Issue issue = new Issue("PAY-123", "Fix payment timeout", null, null, null, null);
        assertEquals("PAY-123 Fix payment timeout in payment service",
                IssueCommitMessage.applyIssueKey("Fix payment timeout in payment service", issue, true));
    }

    @Test
    void doesNotDuplicateKey() {
        Issue issue = new Issue("PAY-123", "Fix payment timeout", null, null, null, null);
        assertEquals("PAY-123 already there",
                IssueCommitMessage.applyIssueKey("PAY-123 already there", issue, true));
    }

    @Test
    void emptyMessage_usesTitle() {
        Issue issue = new Issue("PAY-123", "Fix payment timeout", null, null, null, null);
        assertEquals("PAY-123 Fix payment timeout",
                IssueCommitMessage.applyIssueKey("  ", issue, true));
    }

    @Test
    void skipWhenUncheckedOrNoIssue() {
        Issue issue = new Issue("PAY-123", "Fix payment timeout", null, null, null, null);
        assertEquals("plain", IssueCommitMessage.applyIssueKey("plain", issue, false));
        assertEquals("plain", IssueCommitMessage.applyIssueKey("plain", Issue.none(), true));
        assertEquals("plain", IssueCommitMessage.applyIssueKey("plain", null, true));
    }
}
