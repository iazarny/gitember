package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueStatus;
import com.az.gitember.data.IssueTrackerConfig;
import com.az.gitember.data.TrackerProject;
import com.az.gitember.service.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jira Cloud / Data Center via REST. Credentials never leave this machine except
 * as HTTP Basic to the configured Jira server.
 */
public class JiraIssueTracker implements IssueTracker {

    private static final Logger log = Logger.getLogger(JiraIssueTracker.class.getName());
    private static final int SEARCH_LIMIT = 40;

    private final IssueTrackerConfig config;
    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public JiraIssueTracker(IssueTrackerConfig config) {
        this.config = config;
        this.baseUrl = normalizeServer(config.getServer());
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.mapper = Context.getObjectMapper();
    }

    @Override
    public String getName() {
        return "Jira";
    }

    @Override
    public List<Issue> search(String query) throws Exception {
        String jql = buildJql(query, config.getProjectKeys());
        String encoded = URLEncoder.encode(jql, StandardCharsets.UTF_8);
        String body = get("/rest/api/3/search/jql?jql=" + encoded
                + "&maxResults=" + SEARCH_LIMIT
                + "&fields=summary,description,status,assignee");
        return parseSearch(body);
    }

    @Override
    public Optional<Issue> getIssue(String key) throws Exception {
        Optional<Issue> found = Optional.empty();
        if (key != null && !key.isBlank()) {
            String body = get("/rest/api/2/issue/" + URLEncoder.encode(key.trim(), StandardCharsets.UTF_8)
                    + "?fields=summary,description,status,assignee");
            Issue issue = parseIssue(mapper.readTree(body));
            if (issue != null && issue.isPresent()) {
                found = Optional.of(issue);
            }
        }
        return found;
    }

    @Override
    public void createIssue(Issue issue) throws Exception {
        if (issue == null || issue.getTitle() == null || issue.getTitle().isBlank()) {
            throw new IllegalArgumentException("Issue title is required");
        }
        List<String> projects = config.getProjectKeys();
        if (projects.isEmpty()) {
            throw new IllegalStateException("Select at least one Jira project in Settings");
        }
        ObjectNode fields = mapper.createObjectNode();
        ObjectNode project = mapper.createObjectNode();
        project.put("key", projects.get(0));
        fields.set("project", project);
        fields.put("summary", issue.getTitle());
        if (issue.getDescription() != null && !issue.getDescription().isBlank()) {
            fields.put("description", issue.getDescription());
        }
        ObjectNode type = mapper.createObjectNode();
        type.put("name", "Task");
        fields.set("issuetype", type);
        ObjectNode payload = mapper.createObjectNode();
        payload.set("fields", fields);
        post("/rest/api/2/issue", mapper.writeValueAsString(payload));
    }

    @Override
    public void addComment(String issueKey, String comment) throws Exception {
        if (issueKey == null || issueKey.isBlank() || comment == null) {
            throw new IllegalArgumentException("Issue key and comment are required");
        }
        ObjectNode payload = mapper.createObjectNode();
        payload.put("body", comment);
        post("/rest/api/2/issue/" + encodeKey(issueKey) + "/comment", mapper.writeValueAsString(payload));
    }

    @Override
    public List<IssueStatus> getStatuses(String issueKey) throws Exception {
        String body = get("/rest/api/2/issue/" + encodeKey(issueKey) + "/transitions");
        List<IssueStatus> statuses = new ArrayList<>();
        JsonNode transitions = mapper.readTree(body).path("transitions");
        if (transitions.isArray()) {
            for (JsonNode t : transitions) {
                statuses.add(new IssueStatus(t.path("id").asText(""), t.path("name").asText("")));
            }
        }
        return statuses;
    }

    @Override
    public void transition(String issueKey, String transitionId) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        ObjectNode transition = mapper.createObjectNode();
        transition.put("id", transitionId);
        payload.set("transition", transition);
        post("/rest/api/2/issue/" + encodeKey(issueKey) + "/transitions", mapper.writeValueAsString(payload));
    }

    @Override
    public String getIssueUrl(String issueKey) {
        String url = "";
        if (baseUrl != null && issueKey != null && !issueKey.isBlank()) {
            url = baseUrl + "/browse/" + issueKey.trim();
        }
        return url;
    }

    @Override
    public List<TrackerProject> listProjects() throws Exception {
        String body = get("/rest/api/2/project");
        return parseProjects(body);
    }

    @Override
    public String testConnection() throws Exception {
        String body = get("/rest/api/2/myself");
        JsonNode me = mapper.readTree(body);
        String name = me.path("displayName").asText("");
        String email = me.path("emailAddress").asText("");
        String who = name;
        if (who.isBlank()) {
            who = email;
        }
        if (who.isBlank()) {
            who = "OK";
        }
        return who;
    }

    static String buildJql(String query, List<String> projectKeys) {
        StringJoiner parts = new StringJoiner(" AND ");
        if (projectKeys != null && !projectKeys.isEmpty()) {
            StringJoiner keys = new StringJoiner(", ", "project in (", ")");
            for (String key : projectKeys) {
                if (key != null && !key.isBlank()) {
                    keys.add(key.trim());
                }
            }
            String in = keys.toString();
            if (!in.equals("project in ()")) {
                parts.add(in);
            }
        }
        String trimmed = query != null ? query.trim() : "";
        if (trimmed.isEmpty()) {
            parts.add("assignee = currentUser()");
            parts.add("resolution = Unresolved");
        } else {
            String detected = IssueKeyDetector.findFirst(trimmed.toUpperCase());
            if (detected != null && detected.equalsIgnoreCase(trimmed)) {
                parts.add("key = " + detected);
            } else {
                String escaped = trimmed.replace("\\", "\\\\").replace("\"", "\\\"");
                parts.add("(summary ~ \"" + escaped + "\" OR description ~ \"" + escaped + "\" OR key = \"" + escaped + "\")");
            }
        }
        String jql = parts.toString();
        if (jql.isEmpty()) {
            jql = "assignee = currentUser() AND resolution = Unresolved";
        }
        return jql + " ORDER BY updated DESC";
    }

    List<Issue> parseSearch(String body) throws Exception {
        List<Issue> issues = new ArrayList<>();
        JsonNode root = mapper.readTree(body);
        JsonNode list = root.path("issues");
        if (list.isArray()) {
            for (JsonNode node : list) {
                Issue issue = parseIssue(node);
                if (issue != null && issue.isPresent()) {
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    Issue parseIssue(JsonNode node) {
        Issue issue = null;
        if (node != null && !node.isMissingNode()) {
            String key = node.path("key").asText("");
            JsonNode fields = node.path("fields");
            String title = fields.path("summary").asText("");
            String description = descriptionText(fields.get("description"));
            String status = fields.path("status").path("name").asText("");
            String assignee = fields.path("assignee").path("displayName").asText("");
            issue = new Issue(key, title, description, status, assignee, getIssueUrl(key));
        }
        return issue;
    }

    static String descriptionText(JsonNode description) {
        String text = "";
        if (description != null && !description.isNull() && !description.isMissingNode()) {
            if (description.isTextual()) {
                text = description.asText("");
            } else {
                StringBuilder sb = new StringBuilder();
                collectText(description, sb);
                text = sb.toString().trim();
            }
        }
        return text;
    }

    static void collectText(JsonNode node, StringBuilder sb) {
        if (node != null) {
            if (node.isTextual()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(node.asText());
            } else if (node.has("text")) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(node.path("text").asText(""));
            }
            JsonNode content = node.path("content");
            if (content.isArray()) {
                for (JsonNode child : content) {
                    collectText(child, sb);
                }
            }
        }
    }

    List<TrackerProject> parseProjects(String body) throws Exception {
        List<TrackerProject> projects = new ArrayList<>();
        JsonNode root = mapper.readTree(body);
        JsonNode list = root;
        if (root.isObject() && root.has("values")) {
            list = root.path("values");
        }
        if (list.isArray()) {
            for (JsonNode n : list) {
                String key = n.path("key").asText("");
                if (!key.isBlank()) {
                    projects.add(new TrackerProject(key, n.path("name").asText("")));
                }
            }
        }
        return projects;
    }

    private String get(String path) throws Exception {
        HttpRequest request = authorized(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(25))
                .GET())
                .build();
        return send(request);
    }

    private String post(String path, String json) throws Exception {
        HttpRequest request = authorized(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)))
                .build();
        return send(request);
    }

    private HttpRequest.Builder authorized(HttpRequest.Builder builder) {
        String email = config.getEmail() != null ? config.getEmail() : "";
        String token = config.getApiToken() != null ? config.getApiToken() : "";
        String basic = Base64.getEncoder().encodeToString(
                (email + ":" + token).getBytes(StandardCharsets.UTF_8));
        return builder
                .header("Authorization", "Basic " + basic)
                .header("Accept", "application/json");
    }

    private String send(HttpRequest request) throws Exception {
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            String snippet = response.body() != null && response.body().length() > 300
                    ? response.body().substring(0, 300) : response.body();
            log.log(Level.WARNING, "Jira HTTP " + status + " " + request.uri() + " " + snippet);
            throw new java.io.IOException("Jira HTTP " + status + (snippet != null && !snippet.isBlank()
                    ? ": " + snippet : ""));
        }
        return response.body() != null ? response.body() : "";
    }

    private static String encodeKey(String key) {
        return URLEncoder.encode(key.trim(), StandardCharsets.UTF_8);
    }

    static String normalizeServer(String server) {
        String url = server != null ? server.trim() : "";
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.isEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }
}
