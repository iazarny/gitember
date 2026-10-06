package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueTrackerConfig;
import com.az.gitember.data.TrackerProject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JiraIssueTrackerTest {

    private JiraIssueTracker tracker() {
        IssueTrackerConfig cfg = new IssueTrackerConfig();
        cfg.setEnabled(true);
        cfg.setServer("https://company.atlassian.net");
        cfg.setEmail("dev@company.com");
        cfg.setApiToken("token");
        return new JiraIssueTracker(cfg);
    }

    @Test
    void buildJql_assignedOpen_withProjects() {
        String jql = JiraIssueTracker.buildJql("", List.of("PAY", "CORE"));
        assertTrue(jql.startsWith("project in (PAY, CORE) AND assignee = currentUser() AND resolution = Unresolved"));
        assertTrue(jql.endsWith("ORDER BY updated DESC"));
    }

    @Test
    void buildJql_exactKey() {
        String jql = JiraIssueTracker.buildJql("PAY-123", List.of());
        assertTrue(jql.contains("key = PAY-123"));
        assertFalse(jql.contains("summary ~"));
    }

    @Test
    void buildJql_textSearch() {
        String jql = JiraIssueTracker.buildJql("payment timeout", List.of("PAY"));
        assertTrue(jql.contains("project in (PAY)"));
        assertTrue(jql.contains("summary ~ \"payment timeout\""));
    }

    @Test
    void normalizeServer_stripsSlashAndAddsHttps() {
        assertEquals("https://company.atlassian.net",
                JiraIssueTracker.normalizeServer("https://company.atlassian.net/"));
        assertEquals("https://jira.example.com",
                JiraIssueTracker.normalizeServer("jira.example.com"));
    }

    @Test
    void parseSearch_andIssueUrl() throws Exception {
        JiraIssueTracker tracker = tracker();
        String json = """
                {
                  "issues": [
                    {
                      "key": "PAY-123",
                      "fields": {
                        "summary": "Fix payment timeout",
                        "description": "Hangs after 30s",
                        "status": { "name": "In Progress" },
                        "assignee": { "displayName": "Ada" }
                      }
                    }
                  ]
                }
                """;
        List<Issue> issues = tracker.parseSearch(json);
        assertEquals(1, issues.size());
        Issue issue = issues.get(0);
        assertEquals("PAY-123", issue.getKey());
        assertEquals("Fix payment timeout", issue.getTitle());
        assertEquals("Hangs after 30s", issue.getDescription());
        assertEquals("In Progress", issue.getStatus());
        assertEquals("Ada", issue.getAssignee());
        assertEquals("https://company.atlassian.net/browse/PAY-123", issue.getUrl());
    }

    @Test
    void descriptionText_walksAdf() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String adf = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        { "type": "text", "text": "First" },
                        { "type": "text", "text": "line" }
                      ]
                    }
                  ]
                }
                """;
        assertEquals("First line", JiraIssueTracker.descriptionText(mapper.readTree(adf)));
        assertEquals("plain", JiraIssueTracker.descriptionText(mapper.readTree("\"plain\"")));
        assertEquals("", JiraIssueTracker.descriptionText(null));
    }

    @Test
    void parseProjects_arrayAndPaged() throws Exception {
        JiraIssueTracker tracker = tracker();
        List<TrackerProject> fromArray = tracker.parseProjects(
                "[{\"key\":\"PAY\",\"name\":\"Payments\"},{\"key\":\"CORE\",\"name\":\"Core\"}]");
        assertEquals(2, fromArray.size());
        assertEquals("PAY", fromArray.get(0).getKey());
        assertEquals("Payments", fromArray.get(0).getName());

        List<TrackerProject> paged = tracker.parseProjects(
                "{\"values\":[{\"key\":\"WEB\",\"name\":\"Web\"}]}");
        assertEquals(1, paged.size());
        assertEquals("WEB", paged.get(0).getKey());
    }

    @Test
    void getIssueUrl() {
        assertEquals("https://company.atlassian.net/browse/PAY-123",
                tracker().getIssueUrl("PAY-123"));
    }
}
