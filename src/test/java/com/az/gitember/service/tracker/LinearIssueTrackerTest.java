package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueTrackerConfig;
import com.az.gitember.data.TrackerProject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinearIssueTrackerTest {

    private LinearIssueTracker tracker() {
        IssueTrackerConfig cfg = new IssueTrackerConfig();
        cfg.setEnabled(true);
        cfg.setType(IssueTrackerConfig.TYPE_LINEAR);
        cfg.setApiToken("lin_api_test");
        cfg.setServer("https://linear.app/acme");
        return new LinearIssueTracker(cfg);
    }

    @Test
    void assignedOpenFilter_includesMeStateAndTeams() throws Exception {
        JsonNode filter = LinearIssueTracker.assignedOpenFilter(List.of("PAY", "ENG"));
        assertTrue(filter.path("assignee").path("isMe").path("eq").asBoolean());
        assertEquals("completed", filter.path("state").path("type").path("nin").get(0).asText());
        assertEquals("PAY", filter.path("team").path("key").path("in").get(0).asText());
        assertEquals("ENG", filter.path("team").path("key").path("in").get(1).asText());
    }

    @Test
    void teamFilter_emptyWhenNoKeys() {
        assertNull(LinearIssueTracker.teamFilter(List.of()));
        assertNull(LinearIssueTracker.teamFilter(null));
    }

    @Test
    void parseIssue_andUrlFallback() throws Exception {
        LinearIssueTracker tracker = tracker();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("""
                {
                  "identifier": "PAY-123",
                  "title": "Fix payment timeout",
                  "description": "Hangs after 30s",
                  "url": "https://linear.app/acme/issue/PAY-123",
                  "state": { "name": "In Progress" },
                  "assignee": { "name": "Ada", "displayName": "Ada Lovelace" }
                }
                """);
        Issue issue = tracker.parseIssue(node);
        assertEquals("PAY-123", issue.getKey());
        assertEquals("Fix payment timeout", issue.getTitle());
        assertEquals("Hangs after 30s", issue.getDescription());
        assertEquals("In Progress", issue.getStatus());
        assertEquals("Ada Lovelace", issue.getAssignee());
        assertEquals("https://linear.app/acme/issue/PAY-123", issue.getUrl());
    }

    @Test
    void parseIssueNodes_skipsEmpty() throws Exception {
        LinearIssueTracker tracker = tracker();
        ObjectMapper mapper = new ObjectMapper();
        List<Issue> issues = tracker.parseIssueNodes(mapper.readTree("""
                [
                  { "identifier": "PAY-1", "title": "One" },
                  { "identifier": "", "title": "skip" }
                ]
                """));
        assertEquals(1, issues.size());
        assertEquals("PAY-1", issues.get(0).getKey());
    }

    @Test
    void parseTeams() throws Exception {
        LinearIssueTracker tracker = tracker();
        ObjectMapper mapper = new ObjectMapper();
        List<TrackerProject> teams = tracker.parseTeams(mapper.readTree("""
                [
                  { "key": "PAY", "name": "Payments" },
                  { "key": "ENG", "name": "Engineering" }
                ]
                """));
        assertEquals(2, teams.size());
        assertEquals("PAY", teams.get(0).getKey());
        assertEquals("Payments", teams.get(0).getName());
    }

    @Test
    void getIssueUrl_fromWorkspace() {
        assertEquals("https://linear.app/acme/issue/PAY-123",
                tracker().getIssueUrl("PAY-123"));
    }
}
