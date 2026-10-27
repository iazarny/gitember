package com.az.gitember.data;

import com.az.gitember.service.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueTrackerConfigJsonTest {

    @Test
    void projectRoundTrip_masksApiToken() throws Exception {
        String secret = "super-secret-jira-token";
        Project project = new Project("C:\\dev\\p", new Date());
        IssueTrackerConfig cfg = new IssueTrackerConfig();
        cfg.setEnabled(true);
        cfg.setType(IssueTrackerConfig.TYPE_JIRA);
        cfg.setServer("https://company.atlassian.net");
        cfg.setAuthType(IssueTrackerConfig.AUTH_API_TOKEN);
        cfg.setEmail("dev@company.com");
        cfg.setApiToken(secret);
        cfg.setProjectKeys(List.of("PAY", "CORE"));
        cfg.setIncludeIssueKeyInCommit(true);
        cfg.setCommentOnCommit(true);
        cfg.setBranchNamePattern("{type}/{key}-{slug}");
        cfg.setBranchType("feature");
        project.setIssueTracker(cfg);

        ObjectMapper mapper = Context.getObjectMapper();
        String json = mapper.writeValueAsString(project);
        JsonNode tracker = mapper.readTree(json).path("issueTracker");

        assertFalse(json.contains(secret), "API token must not be stored in plain text");
        assertEquals("https://company.atlassian.net", tracker.path("server").asText());
        assertEquals("dev@company.com", tracker.path("email").asText());
        assertTrue(tracker.path("enabled").asBoolean());
        assertEquals("PAY", tracker.path("projectKeys").get(0).asText());

        Project back = mapper.readValue(json, Project.class);
        IssueTrackerConfig restored = back.getIssueTracker();
        assertEquals(secret, restored.getApiToken());
        assertEquals(List.of("PAY", "CORE"), restored.getProjectKeys());
        assertTrue(restored.isCommentOnCommit());
        assertTrue(restored.isReady());
    }

    @Test
    void settingsRoot_doesNotStoreIssueTracker() throws Exception {
        Settings settings = new Settings();
        settings.getProjects().add(new Project("C:\\dev\\p", new Date()));
        String json = Context.getObjectMapper().writeValueAsString(settings);
        JsonNode root = Context.getObjectMapper().readTree(json);
        assertFalse(root.has("issueTracker"), "issue tracker belongs on Project, not Settings");
        assertTrue(root.path("projects").get(0).has("issueTracker"));
    }

    @Test
    void isReady_requiresEnabledServerTokenAndEmail() {
        IssueTrackerConfig cfg = new IssueTrackerConfig();
        assertFalse(cfg.isReady());
        cfg.setEnabled(true);
        cfg.setServer("https://company.atlassian.net");
        cfg.setApiToken("tok");
        assertFalse(cfg.isReady());
        cfg.setEmail("dev@company.com");
        assertTrue(cfg.isReady());
    }

    @Test
    void isReady_linearNeedsOnlyApiKey() {
        IssueTrackerConfig cfg = new IssueTrackerConfig();
        cfg.setType(IssueTrackerConfig.TYPE_LINEAR);
        cfg.setEnabled(true);
        assertFalse(cfg.isReady());
        cfg.setApiToken("lin_api_secret");
        assertTrue(cfg.isReady());
    }
}
