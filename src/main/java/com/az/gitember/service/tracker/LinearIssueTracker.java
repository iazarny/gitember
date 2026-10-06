package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueStatus;
import com.az.gitember.data.IssueTrackerConfig;
import com.az.gitember.data.TrackerProject;
import com.az.gitember.service.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Linear via GraphQL. Personal API key stays on this machine and is sent only to
 * {@code https://api.linear.app/graphql}.
 */
public class LinearIssueTracker implements IssueTracker {

    private static final Logger log = Logger.getLogger(LinearIssueTracker.class.getName());
    static final String GRAPHQL_URL = "https://api.linear.app/graphql";
    private static final int SEARCH_LIMIT = 40;

    private static final String ISSUE_FIELDS = """
            id identifier title description url
            state { name }
            assignee { name displayName }
            """;

    private final IssueTrackerConfig config;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private String organizationUrlKey = "";

    public LinearIssueTracker(IssueTrackerConfig config) {
        this.config = config;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.mapper = Context.getObjectMapper();
    }

    @Override
    public String getName() {
        return "Linear";
    }

    @Override
    public List<Issue> search(String query) throws Exception {
        String trimmed = query != null ? query.trim() : "";
        String key = IssueKeyDetector.findFirst(trimmed.toUpperCase());
        List<Issue> issues;
        if (!trimmed.isEmpty() && key != null && key.equalsIgnoreCase(trimmed)) {
            Optional<Issue> one = getIssue(key);
            issues = new ArrayList<>();
            one.ifPresent(issues::add);
        } else if (trimmed.isEmpty()) {
            ObjectNode vars = mapper.createObjectNode();
            vars.put("first", SEARCH_LIMIT);
            vars.set("filter", assignedOpenFilter(config.getProjectKeys()));
            JsonNode data = graphql(
                    "query Assigned($first: Int!, $filter: IssueFilter) { "
                            + "issues(filter: $filter, first: $first, orderBy: updatedAt) { "
                            + "nodes { " + ISSUE_FIELDS + " } } }",
                    vars);
            issues = parseIssueNodes(data.path("issues").path("nodes"));
        } else {
            ObjectNode vars = mapper.createObjectNode();
            vars.put("term", trimmed);
            vars.put("first", SEARCH_LIMIT);
            ObjectNode filter = teamFilter(config.getProjectKeys());
            if (filter != null) {
                vars.set("filter", filter);
            }
            JsonNode data = graphql(
                    "query Search($term: String!, $first: Int, $filter: IssueFilter) { "
                            + "searchIssues(term: $term, first: $first, filter: $filter) { "
                            + "nodes { " + ISSUE_FIELDS + " } } }",
                    vars);
            issues = parseIssueNodes(data.path("searchIssues").path("nodes"));
        }
        return issues;
    }

    @Override
    public Optional<Issue> getIssue(String key) throws Exception {
        Optional<Issue> found = Optional.empty();
        if (key != null && !key.isBlank()) {
            ObjectNode vars = mapper.createObjectNode();
            vars.put("id", key.trim());
            JsonNode data = graphql(
                    "query One($id: String!) { issue(id: $id) { " + ISSUE_FIELDS + " } }",
                    vars);
            Issue issue = parseIssue(data.path("issue"));
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
        List<String> teams = config.getProjectKeys();
        if (teams.isEmpty()) {
            throw new IllegalStateException("Select at least one Linear team in Project Settings");
        }
        String teamId = resolveTeamId(teams.get(0));
        ObjectNode input = mapper.createObjectNode();
        input.put("title", issue.getTitle());
        input.put("teamId", teamId);
        if (issue.getDescription() != null && !issue.getDescription().isBlank()) {
            input.put("description", issue.getDescription());
        }
        ObjectNode vars = mapper.createObjectNode();
        vars.set("input", input);
        graphql("""
                mutation Create($input: IssueCreateInput!) {
                  issueCreate(input: $input) { success issue { identifier } }
                }
                """, vars);
    }

    @Override
    public void addComment(String issueKey, String comment) throws Exception {
        if (issueKey == null || issueKey.isBlank() || comment == null) {
            throw new IllegalArgumentException("Issue key and comment are required");
        }
        String issueId = resolveIssueId(issueKey);
        ObjectNode vars = mapper.createObjectNode();
        vars.put("issueId", issueId);
        vars.put("body", comment);
        graphql("""
                mutation Comment($issueId: String!, $body: String!) {
                  commentCreate(input: { issueId: $issueId, body: $body }) { success }
                }
                """, vars);
    }

    @Override
    public List<IssueStatus> getStatuses(String issueKey) throws Exception {
        ObjectNode vars = mapper.createObjectNode();
        vars.put("id", issueKey != null ? issueKey.trim() : "");
        JsonNode data = graphql("""
                query States($id: String!) {
                  issue(id: $id) {
                    team { states { nodes { id name } } }
                  }
                }
                """, vars);
        List<IssueStatus> statuses = new ArrayList<>();
        JsonNode nodes = data.path("issue").path("team").path("states").path("nodes");
        if (nodes.isArray()) {
            for (JsonNode n : nodes) {
                statuses.add(new IssueStatus(n.path("id").asText(""), n.path("name").asText("")));
            }
        }
        return statuses;
    }

    @Override
    public void transition(String issueKey, String transitionId) throws Exception {
        String issueId = resolveIssueId(issueKey);
        ObjectNode vars = mapper.createObjectNode();
        vars.put("id", issueId);
        ObjectNode input = mapper.createObjectNode();
        input.put("stateId", transitionId);
        vars.set("input", input);
        graphql("""
                mutation Move($id: String!, $input: IssueUpdateInput!) {
                  issueUpdate(id: $id, input: $input) { success }
                }
                """, vars);
    }

    @Override
    public String getIssueUrl(String issueKey) {
        String url = "";
        if (issueKey != null && !issueKey.isBlank()) {
            String workspace = browseBase();
            if (!workspace.isBlank()) {
                url = workspace + "/issue/" + issueKey.trim();
            }
        }
        return url;
    }

    @Override
    public List<TrackerProject> listProjects() throws Exception {
        JsonNode data = graphql("""
                query Teams {
                  teams(first: 50) { nodes { id key name } }
                }
                """, null);
        return parseTeams(data.path("teams").path("nodes"));
    }

    @Override
    public String testConnection() throws Exception {
        JsonNode data = graphql("""
                query Me {
                  viewer { name displayName email }
                  organization { urlKey }
                }
                """, null);
        organizationUrlKey = data.path("organization").path("urlKey").asText("");
        JsonNode viewer = data.path("viewer");
        String who = viewer.path("displayName").asText("");
        if (who.isBlank()) {
            who = viewer.path("name").asText("");
        }
        if (who.isBlank()) {
            who = viewer.path("email").asText("");
        }
        if (who.isBlank()) {
            who = "OK";
        }
        return who;
    }

    public String getOrganizationUrl() {
        String url = browseBase();
        if (url.isBlank() && organizationUrlKey != null && !organizationUrlKey.isBlank()) {
            url = "https://linear.app/" + organizationUrlKey;
        }
        return url;
    }

    static ObjectNode assignedOpenFilter(List<String> teamKeys) {
        ObjectMapper m = new ObjectMapper();
        ObjectNode filter = m.createObjectNode();
        ObjectNode isMe = m.createObjectNode();
        isMe.put("eq", true);
        ObjectNode assignee = m.createObjectNode();
        assignee.set("isMe", isMe);
        filter.set("assignee", assignee);
        ArrayNode nin = m.createArrayNode();
        nin.add("completed");
        nin.add("canceled");
        ObjectNode type = m.createObjectNode();
        type.set("nin", nin);
        ObjectNode state = m.createObjectNode();
        state.set("type", type);
        filter.set("state", state);
        ObjectNode team = teamFilter(teamKeys);
        if (team != null) {
            filter.set("team", team.get("team"));
        }
        return filter;
    }

    static ObjectNode teamFilter(List<String> teamKeys) {
        ObjectNode wrapper = null;
        if (teamKeys != null && !teamKeys.isEmpty()) {
            ObjectMapper m = new ObjectMapper();
            ArrayNode keys = m.createArrayNode();
            for (String key : teamKeys) {
                if (key != null && !key.isBlank()) {
                    keys.add(key.trim());
                }
            }
            if (keys.size() > 0) {
                ObjectNode in = m.createObjectNode();
                in.set("in", keys);
                ObjectNode key = m.createObjectNode();
                key.set("key", in);
                wrapper = m.createObjectNode();
                wrapper.set("team", key);
            }
        }
        return wrapper;
    }

    List<Issue> parseIssueNodes(JsonNode nodes) {
        List<Issue> issues = new ArrayList<>();
        if (nodes != null && nodes.isArray()) {
            for (JsonNode node : nodes) {
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
        if (node != null && !node.isMissingNode() && !node.isNull()) {
            String key = node.path("identifier").asText("");
            String title = node.path("title").asText("");
            String description = node.path("description").asText("");
            String status = node.path("state").path("name").asText("");
            JsonNode assignee = node.path("assignee");
            String who = assignee.path("displayName").asText("");
            if (who.isBlank()) {
                who = assignee.path("name").asText("");
            }
            String url = node.path("url").asText("");
            if (url.isBlank()) {
                url = getIssueUrl(key);
            }
            issue = new Issue(key, title, description, status, who, url);
        }
        return issue;
    }

    List<TrackerProject> parseTeams(JsonNode nodes) {
        List<TrackerProject> teams = new ArrayList<>();
        if (nodes != null && nodes.isArray()) {
            for (JsonNode n : nodes) {
                String key = n.path("key").asText("");
                if (!key.isBlank()) {
                    teams.add(new TrackerProject(key, n.path("name").asText("")));
                }
            }
        }
        return teams;
    }

    private String resolveIssueId(String issueKey) throws Exception {
        ObjectNode vars = mapper.createObjectNode();
        vars.put("id", issueKey.trim());
        JsonNode data = graphql("query Id($id: String!) { issue(id: $id) { id } }", vars);
        String id = data.path("issue").path("id").asText("");
        if (id.isBlank()) {
            throw new java.io.IOException("Linear issue not found: " + issueKey);
        }
        return id;
    }

    private String resolveTeamId(String teamKey) throws Exception {
        ObjectNode vars = mapper.createObjectNode();
        vars.put("id", teamKey);
        JsonNode data = graphql("query Team($id: String!) { team(id: $id) { id } }", vars);
        String id = data.path("team").path("id").asText("");
        if (id.isBlank()) {
            throw new java.io.IOException("Linear team not found: " + teamKey);
        }
        return id;
    }

    private String browseBase() {
        String url = "";
        if (config.getServer() != null && !config.getServer().isBlank()) {
            url = config.getServer().trim();
            while (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
        } else if (organizationUrlKey != null && !organizationUrlKey.isBlank()) {
            url = "https://linear.app/" + organizationUrlKey;
        }
        return url;
    }

    JsonNode graphql(String query, ObjectNode variables) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("query", query);
        if (variables != null) {
            payload.set("variables", variables);
        }
        String token = config.getApiToken() != null ? config.getApiToken() : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GRAPHQL_URL))
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String body = response.body() != null ? response.body() : "";
        if (status < 200 || status >= 300) {
            String snippet = body.length() > 300 ? body.substring(0, 300) : body;
            log.log(Level.WARNING, "Linear HTTP " + status + " " + snippet);
            throw new java.io.IOException("Linear HTTP " + status
                    + (snippet.isBlank() ? "" : ": " + snippet));
        }
        JsonNode root = mapper.readTree(body);
        JsonNode errors = root.path("errors");
        if (errors.isArray() && errors.size() > 0) {
            String message = errors.get(0).path("message").asText("request failed");
            throw new java.io.IOException("Linear: " + message);
        }
        return root.path("data");
    }
}
