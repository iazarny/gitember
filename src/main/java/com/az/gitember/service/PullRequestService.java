package com.az.gitember.service;

import com.az.gitember.data.PullRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches pull requests / merge requests from GitHub or GitLab using their REST APIs.
 * Returns an empty list silently when no token is available or the host is unsupported.
 */
public class PullRequestService {

    private static final Logger log = Logger.getLogger(PullRequestService.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private enum Host { GITHUB, GITLAB, UNKNOWN }

    /**
     * Fetches open PRs for the repository identified by {@code remoteUrl}.
     *
     * @param remoteUrl   git remote URL (HTTPS or SSH)
     * @param accessToken project-level token (may be null; env fallback is tried)
     */
    public static List<PullRequest> fetch(String remoteUrl, String accessToken) {
        if (remoteUrl == null || remoteUrl.isBlank()) return Collections.emptyList();

        Host host = detectHost(remoteUrl);
        if (host == Host.UNKNOWN) return Collections.emptyList();

        String token = resolveToken(accessToken, host);
        if (token == null || token.isBlank()) return Collections.emptyList();

        try {
            return switch (host) {
                case GITHUB -> fetchGitHub(remoteUrl, token);
                case GITLAB -> fetchGitLab(remoteUrl, token);
                default     -> Collections.emptyList();
            };
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to fetch pull requests from " + remoteUrl, e);
            return Collections.emptyList();
        }
    }

    // ---- host detection ----

    private static Host detectHost(String url) {
        if (url.contains("github.com"))                     return Host.GITHUB;
        if (url.contains("gitlab.com") || url.contains("gitlab.")) return Host.GITLAB;
        return Host.UNKNOWN;
    }

    /** Project token first; fall back to well-known environment variable. */
    private static String resolveToken(String projectToken, Host host) {
        if (projectToken != null && !projectToken.isBlank()) return projectToken;
        return switch (host) {
            case GITHUB -> System.getenv("GITHUB_TOKEN");
            case GITLAB -> System.getenv("GITLAB_TOKEN");
            default     -> null;
        };
    }

    // ---- URL parsing ----

    /**
     * Extracts the path segments after the host domain from an HTTPS or SSH remote URL.
     * Returns null if parsing fails.
     *
     * Examples:
     *   https://github.com/owner/repo.git  → ["owner","repo"]
     *   git@github.com:owner/repo.git      → ["owner","repo"]
     *   https://gitlab.com/g/sub/repo.git  → ["g","sub","repo"]
     */
    private static String[] parsePathSegments(String url, String hostDomain) {
        String u = url.replaceAll("\\.git$", "");
        String path = null;

        // HTTPS / HTTPS with credentials
        if (u.contains(hostDomain + "/")) {
            int idx = u.indexOf(hostDomain + "/");
            path = u.substring(idx + hostDomain.length() + 1);
        }
        // SSH  git@host:path  or  ssh://git@host/path
        else if (u.contains(":")) {
            path = u.substring(u.lastIndexOf(':') + 1);
            if (path.startsWith("/")) path = path.substring(1);
        }

        if (path == null || path.isBlank()) return null;
        String[] parts = path.split("/");
        return parts.length >= 2 ? parts : null;
    }

    // ---- GitHub ----

    private static List<PullRequest> fetchGitHub(String remoteUrl, String token) throws Exception {
        String[] parts = parsePathSegments(remoteUrl, "github.com");
        if (parts == null) return Collections.emptyList();

        String apiUrl = "https://api.github.com/repos/" + parts[0] + "/" + parts[1]
                + "/pulls?state=open&per_page=100";

        String body = get(apiUrl,
                "Authorization",       "Bearer " + token,
                "Accept",              "application/vnd.github+json",
                "X-GitHub-Api-Version","2022-11-28");
        if (body == null) return Collections.emptyList();

        List<PullRequest> result = new ArrayList<>();
        for (JsonNode pr : MAPPER.readTree(body)) {
            result.add(new PullRequest(
                    pr.get("number").asInt(),
                    pr.get("title").asText(""),
                    pr.path("user").path("login").asText(""),
                    pr.path("head").path("ref").asText(""),
                    pr.path("base").path("ref").asText(""),
                    pr.path("html_url").asText(""),
                    pr.path("state").asText("open")));
        }
        return result;
    }

    // ---- GitLab ----

    private static List<PullRequest> fetchGitLab(String remoteUrl, String token) throws Exception {
        String[] parts = parsePathSegments(remoteUrl, "gitlab.com");
        if (parts == null) return Collections.emptyList();

        // Join all segments with %2F for the project path (handles nested groups)
        String encodedPath = String.join("%2F", parts);
        String apiUrl = "https://gitlab.com/api/v4/projects/" + encodedPath
                + "/merge_requests?state=opened&per_page=100";

        String body = get(apiUrl, "PRIVATE-TOKEN", token);
        if (body == null) return Collections.emptyList();

        List<PullRequest> result = new ArrayList<>();
        for (JsonNode mr : MAPPER.readTree(body)) {
            result.add(new PullRequest(
                    mr.get("iid").asInt(),
                    mr.get("title").asText(""),
                    mr.path("author").path("username").asText(""),
                    mr.path("source_branch").asText(""),
                    mr.path("target_branch").asText(""),
                    mr.path("web_url").asText(""),
                    mr.path("state").asText("opened")));
        }
        return result;
    }

    // ---- HTTP helper ----

    /** Performs a GET request with the supplied header key-value pairs. Returns null on non-200. */
    private static String get(String url, String... headers) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        for (int i = 0; i + 1 < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }

        HttpResponse<String> response = client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warning("PR API returned HTTP " + response.statusCode() + " for " + url);
            return null;
        }
        return response.body();
    }
}
